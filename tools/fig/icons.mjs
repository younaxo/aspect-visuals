#!/usr/bin/env node
// Выгружает иконки из исходника Figma в SVG.
//
// Контуры лежат в блобах последовательностью команд: байт кода и следом
// координаты числами одинарной точности. Коды: 0 — закрыть контур,
// 1 — перенос, 2 — линия, 3 — квадратичная кривая, 4 — кубическая.

import { mkdirSync, writeFileSync } from "node:fs";
import { join, dirname } from "node:path";
import { fileURLToPath } from "node:url";
import { loadFig, index } from "./lib.mjs";

const ARGUMENTS = { 0: 0, 1: 2, 2: 2, 3: 4, 4: 6 };
const LETTERS = { 0: "Z", 1: "M", 2: "L", 3: "Q", 4: "C" };

function pathOf(bytes) {
    const view = Buffer.from(bytes);
    const parts = [];
    let offset = 0;
    while (offset < view.length) {
        const code = view[offset];
        const count = ARGUMENTS[code];
        if (count === undefined) {
            throw new Error(`неизвестная команда контура ${code} на позиции ${offset}`);
        }
        offset += 1;
        const numbers = [];
        for (let i = 0; i < count; i++) {
            numbers.push(round(view.readFloatLE(offset)));
            offset += 4;
        }
        parts.push(LETTERS[code] + numbers.join(" "));
    }
    return parts.join(" ");
}

const round = value => Math.round(value * 1000) / 1000;

/** Матрицы узла и его родителей перемножаются: узел задан относительно родителя. */
function combine(outer, inner) {
    return {
        m00: outer.m00 * inner.m00 + outer.m01 * inner.m10,
        m01: outer.m00 * inner.m01 + outer.m01 * inner.m11,
        m02: outer.m00 * inner.m02 + outer.m01 * inner.m12 + outer.m02,
        m10: outer.m10 * inner.m00 + outer.m11 * inner.m10,
        m11: outer.m10 * inner.m01 + outer.m11 * inner.m11,
        m12: outer.m10 * inner.m02 + outer.m11 * inner.m12 + outer.m12,
    };
}

const IDENTITY = { m00: 1, m01: 0, m02: 0, m10: 0, m11: 1, m12: 0 };

function color(paint) {
    if (!paint || paint.type !== "SOLID" || paint.visible === false) {
        return null;
    }
    const channel = value => Math.round(Math.max(0, Math.min(1, value)) * 255);
    const { r, g, b } = paint.color;
    return "#" + [r, g, b].map(v => channel(v).toString(16).padStart(2, "0")).join("");
}

function collect(node, transform, kids, out) {
    if (node.visible === false) {
        return;
    }
    const local = node.transform ? combine(transform, node.transform) : transform;

    // Обводка приходит уже развёрнутой в контур, поэтому рисуется заливкой:
    // иначе пришлось бы повторять правила стыков и концов линий
    const sources = [
        { geometry: node.fillGeometry, paints: node.fillPaints },
        { geometry: node.strokeGeometry, paints: node.strokePaints },
    ];
    for (const source of sources) {
        const paint = (source.paints || []).find(p => p.visible !== false);
        if (!paint) {
            continue;
        }
        for (const geometry of source.geometry || []) {
            out.push({
                path: pathOf(blobs[geometry.commandsBlob].bytes),
                rule: geometry.windingRule === "ODD" ? "evenodd" : "nonzero",
                fill: color(paint),
                opacity: paint.opacity === undefined ? 1 : paint.opacity,
                transform: local,
            });
        }
    }
    for (const child of kids(node)) {
        collect(child, local, kids, out);
    }
}

const matrix = t => `matrix(${[t.m00, t.m10, t.m01, t.m11, t.m02, t.m12].map(round).join(" ")})`;

function svgOf(node, kids, size) {
    const shapes = [];
    // Сам символ ставится в начало координат: рамка берётся из его размера
    collect(node, IDENTITY, kids, shapes);
    if (shapes.length === 0) {
        return null;
    }
    const body = shapes
        .filter(shape => shape.path)
        .map(shape => `  <path d="${shape.path}" fill="${shape.fill ?? "currentColor"}"`
            + (shape.opacity < 1 ? ` fill-opacity="${round(shape.opacity)}"` : "")
            + ` fill-rule="${shape.rule}" transform="${matrix(shape.transform)}"/>`)
        .join("\n");
    return `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${size.x} ${size.y}"`
        + ` width="${size.x}" height="${size.y}" fill="none">\n${body}\n</svg>\n`;
}

const here = dirname(fileURLToPath(import.meta.url));
const doc = loadFig(process.argv[2]);
const blobs = doc.blobs;
const { kids } = index(doc);

const frames = (process.argv[4] || "Aspect Icons,Flags").split(",");
const target = join(here, "..", "..", process.argv[3] || "src/main/resources/assets/aspectvisuals/icons");
mkdirSync(target, { recursive: true });

let written = 0;
for (const frameName of frames) {
    const frame = doc.nodeChanges.find(x => x.name === frameName && x.type === "FRAME");
    if (!frame) {
        console.error(`фрейм «${frameName}» не найден`);
        continue;
    }
    for (const symbol of kids(frame)) {
        const label = (symbol.name || "").replace(/^Property 1=/, "");
        if (!label) continue;
        const file = label.toLowerCase()
            .replace(/^circle-flags:/, "flag_")
            .replace(/[^a-z0-9]+/g, "_")
            .replace(/^_|_$/g, "");
        const svg = svgOf(symbol, kids, symbol.size || { x: 24, y: 24 });
        if (!svg) {
            console.error(`пропущен без контуров: ${label}`);
            continue;
        }
        writeFileSync(join(target, file + ".svg"), svg);
        written++;
    }
}
console.log(`записано иконок: ${written} -> ${target}`);
