#!/usr/bin/env node
// Сверяет описания core-шейдеров с их исходниками.
//
// Загрузчик Minecraft не жалуется в сборке: несовпадение имени, типа или
// пути обнаруживается только в игре, и весь интерфейс молча уходит на
// запасной путь отрисовки. Проверка ловит это до запуска.

import { readFileSync, existsSync, readdirSync } from "node:fs";
import { join, dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const root = resolve(dirname(fileURLToPath(import.meta.url)), "..");
const assets = join(root, "src/main/resources/assets");

const problems = [];
const fail = (where, message) => problems.push(`${where}: ${message}`);

/** Типы униформ в описании и соответствующие им типы GLSL. */
const TYPES = {
    "float:1": ["float"],
    "float:2": ["vec2"],
    "float:3": ["vec3"],
    "float:4": ["vec4"],
    "int:1": ["int"],
    "int:2": ["ivec2"],
    "int:3": ["ivec3"],
    "int:4": ["ivec4"],
    "matrix3x3:9": ["mat3"],
    "matrix4x4:16": ["mat4"],
};

function stripComments(source) {
    return source.replace(/\/\*[\s\S]*?\*\//g, " ").replace(/\/\/[^\n]*/g, " ");
}

function uniformsOf(source) {
    const found = new Map();
    const pattern = /^\s*uniform\s+(\w+)\s+(\w+)\s*;/gm;
    let match;
    while ((match = pattern.exec(stripComments(source))) !== null) {
        found.set(match[2], match[1]);
    }
    return found;
}

/** "aspectvisuals:core/aspect_shape" -> путь к файлу с указанным расширением. */
function sourcePath(id, extension) {
    const [namespace, path] = id.includes(":") ? id.split(":") : ["minecraft", id];
    return join(assets, namespace, "shaders", `${path}.${extension}`);
}

function checkDefinition(namespace, file) {
    const where = `${namespace}:${file}`;
    const definition = JSON.parse(readFileSync(join(assets, namespace, "shaders/core", file), "utf8"));

    for (const [field, extension] of [["vertex", "vsh"], ["fragment", "fsh"]]) {
        const id = definition[field];
        if (!id) {
            fail(where, `не указан ${field}`);
            continue;
        }
        if (!id.includes("/")) {
            fail(where, `${field} «${id}» без папки: загрузчик ищет путь целиком, а не имя файла`);
        }
        if (!existsSync(sourcePath(id, extension))) {
            fail(where, `${field} «${id}» не найден по пути ${sourcePath(id, extension)}`);
        }
    }

    const vertex = existsSync(sourcePath(definition.vertex ?? "", "vsh"))
        ? uniformsOf(readFileSync(sourcePath(definition.vertex, "vsh"), "utf8")) : new Map();
    const fragment = existsSync(sourcePath(definition.fragment ?? "", "fsh"))
        ? uniformsOf(readFileSync(sourcePath(definition.fragment, "fsh"), "utf8")) : new Map();

    const declared = new Map([...vertex, ...fragment]);
    const samplers = new Set(
        [...declared].filter(([, type]) => type.startsWith("sampler")).map(([name]) => name));

    const listedSamplers = new Set((definition.samplers ?? []).map(entry => entry.name));
    for (const name of samplers) {
        if (!listedSamplers.has(name)) {
            fail(where, `сэмплер ${name} объявлен в GLSL, но отсутствует в описании — текстура не привяжется`);
        }
    }
    for (const name of listedSamplers) {
        if (!samplers.has(name)) {
            fail(where, `сэмплер ${name} перечислен в описании, но не объявлен в GLSL`);
        }
    }

    const listed = new Map((definition.uniforms ?? []).map(entry => [entry.name, entry]));
    for (const [name, entry] of listed) {
        const type = declared.get(name);
        if (type === undefined) {
            fail(where, `униформа ${name} перечислена в описании, но не объявлена в GLSL`);
            continue;
        }
        const expected = TYPES[`${entry.type}:${entry.count}`];
        if (!expected) {
            fail(where, `униформа ${name}: неизвестное сочетание типа «${entry.type}» и количества ${entry.count}`);
        } else if (!expected.includes(type)) {
            fail(where, `униформа ${name}: описание задаёт ${expected.join("/")}, GLSL объявляет ${type}`);
        }
        if (entry.values !== undefined && entry.values.length !== entry.count) {
            fail(where, `униформа ${name}: значений ${entry.values.length} при count ${entry.count}`);
        }
    }
    for (const [name, type] of declared) {
        if (type.startsWith("sampler")) {
            continue;
        }
        if (!listed.has(name)) {
            fail(where, `униформа ${name} объявлена в GLSL, но отсутствует в описании — останется нулевой`);
        }
    }
}

/** Ключи программ из кода должны указывать на существующие описания. */
function checkKeys() {
    const source = join(root, "src/main/java/su/aspectvisuals/client/ui/render/AspectShaders.java");
    const code = readFileSync(source, "utf8");
    const pattern = /AspectVisuals\.id\("([^"]+)"\)/g;
    let match;
    let count = 0;
    while ((match = pattern.exec(code)) !== null) {
        count++;
        const path = join(assets, "aspectvisuals", "shaders", `${match[1]}.json`);
        if (!existsSync(path)) {
            fail("AspectShaders", `ключ «${match[1]}» не имеет описания по пути ${path}`);
        }
    }
    if (count === 0) {
        fail("AspectShaders", "не найдено ни одного ключа программы");
    }
}

for (const namespace of readdirSync(assets)) {
    const directory = join(assets, namespace, "shaders/core");
    if (!existsSync(directory)) {
        continue;
    }
    for (const file of readdirSync(directory).filter(name => name.endsWith(".json"))) {
        checkDefinition(namespace, file);
    }
}
checkKeys();

if (problems.length > 0) {
    console.error("Описания шейдеров расходятся с исходниками:");
    for (const problem of problems) {
        console.error(`  - ${problem}`);
    }
    process.exit(1);
}
console.log("Описания шейдеров согласованы с исходниками.");
