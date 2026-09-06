import { readFileSync, writeFileSync } from "node:fs";
import zlib from "node:zlib";
import { createRequire } from "node:module";
const kiwi = createRequire(import.meta.url)("kiwi-schema");

const raw = readFileSync(process.argv[2]);
if (raw.subarray(0, 8).toString() !== "fig-kiwi") throw new Error("не fig-kiwi");

let offset = 12;
const chunks = [];
while (offset + 4 <= raw.length) {
    const length = raw.readUInt32LE(offset);
    offset += 4;
    if (length === 0 || offset + length > raw.length) break;
    chunks.push(raw.subarray(offset, offset + length));
    offset += length;
}
console.error("чанков:", chunks.length, chunks.map(c => c.length).join(", "));

// Схема сжата deflate, данные — zstd: формат смешанный, поэтому способ
// распаковки выбирается по содержимому, а не по номеру чанка
const inflate = (buf) => {
    if (buf.length > 4 && buf.readUInt32LE(0) === 0xFD2FB528) {
        return zlib.zstdDecompressSync(buf);
    }
    for (const fn of [zlib.inflateRawSync, zlib.inflateSync]) {
        try { return fn(buf); } catch { /* следующий способ */ }
    }
    return buf;
};

const schemaBuf = inflate(chunks[0]);
const dataBuf = inflate(chunks[1]);
console.error("схема:", schemaBuf.length, "данные:", dataBuf.length);

const schema = kiwi.decodeBinarySchema(schemaBuf);
const compiled = kiwi.compileSchema(schema);
const root = schema.definitions.find(d => d.kind === "MESSAGE" && d.name === "Message")
    ?? schema.definitions.find(d => d.kind === "MESSAGE");
console.error("корневое сообщение:", root.name);

const doc = compiled["decode" + root.name](dataBuf);
writeFileSync(process.argv[3], JSON.stringify(doc));
console.error("узлов:", (doc.nodeChanges ?? []).length);
