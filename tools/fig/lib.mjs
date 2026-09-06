import { readFileSync } from "node:fs";
import zlib from "node:zlib";
import { createRequire } from "node:module";
const kiwi = createRequire(import.meta.url)("kiwi-schema");

export function loadFig(path) {
    const raw = readFileSync(path);
    let offset = 12;
    const chunks = [];
    while (offset + 4 <= raw.length) {
        const length = raw.readUInt32LE(offset);
        offset += 4;
        if (length === 0 || offset + length > raw.length) break;
        chunks.push(raw.subarray(offset, offset + length));
        offset += length;
    }
    const inflate = (buf) => buf.length > 4 && buf.readUInt32LE(0) === 0xFD2FB528
        ? zlib.zstdDecompressSync(buf)
        : zlib.inflateRawSync(buf);
    const schema = kiwi.decodeBinarySchema(inflate(chunks[0]));
    const compiled = kiwi.compileSchema(schema);
    return compiled.decodeMessage(inflate(chunks[1]));
}

export function index(doc) {
    const key = g => g ? g.sessionID + ":" + g.localID : null;
    const children = new Map();
    for (const node of doc.nodeChanges) {
        const parent = key(node.parentIndex && node.parentIndex.guid);
        if (!parent) continue;
        if (!children.has(parent)) children.set(parent, []);
        children.get(parent).push(node);
    }
    return { key, children, kids: node => children.get(key(node.guid)) || [] };
}
