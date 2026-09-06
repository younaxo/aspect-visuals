#!/usr/bin/env node
// Печатает раскладку фрейма из макета: размеры, положение, скругления,
// цвета, текст и кегль. Нужен, чтобы собирать интерфейс по числам, а не
// на глаз по картинке.

import { loadFig, index } from "./lib.mjs";

const round = value => Math.round(value * 100) / 100;

function paint(paints) {
    const solid = (paints || []).find(p => p.visible !== false && p.type === "SOLID");
    if (!solid) return "";
    const channel = v => Math.round(Math.max(0, Math.min(1, v)) * 255);
    const { r, g, b, a } = solid.color;
    const alpha = Math.round((a === undefined ? 1 : a) * (solid.opacity === undefined ? 1 : solid.opacity) * 255);
    return " #" + [alpha, channel(r), channel(g), channel(b)]
        .map(v => v.toString(16).padStart(2, "0")).join("").toUpperCase();
}

function describe(node) {
    const parts = [node.type, "«" + (node.name || "") + "»"];
    if (node.size) parts.push(`${round(node.size.x)}x${round(node.size.y)}`);
    if (node.transform) parts.push(`@${round(node.transform.m02)},${round(node.transform.m12)}`);
    const radius = node.cornerRadius ?? node.rectangleTopLeftCornerRadius;
    if (radius) parts.push(`r=${round(radius)}`);
    if (node.fontSize) parts.push(`кегль ${round(node.fontSize)}`);
    if (node.textData && node.textData.characters) {
        parts.push(`текст «${node.textData.characters.replace(/\n/g, " ").slice(0, 40)}»`);
    }
    const fill = paint(node.fillPaints);
    if (fill) parts.push("заливка" + fill);
    const stroke = paint(node.strokePaints);
    if (stroke) parts.push(`обводка${stroke} ${round(node.strokeWeight ?? 1)}`);
    if (node.visible === false) parts.push("(скрыт)");
    return parts.join(" ");
}

const doc = loadFig(process.argv[2]);
const { kids } = index(doc);
const wanted = process.argv[3];
const depth = Number(process.argv[4] || 3);

const matches = doc.nodeChanges.filter(x => x.name === wanted);
if (matches.length === 0) {
    console.error(`не найдено: ${wanted}`);
    process.exit(1);
}

const walk = (node, level) => {
    if (level > depth) return;
    console.log("  ".repeat(level) + describe(node));
    for (const child of kids(node)) walk(child, level + 1);
};
for (const match of matches) walk(match, 0);
