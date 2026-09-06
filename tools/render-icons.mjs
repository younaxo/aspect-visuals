import { Resvg } from '@resvg/resvg-js'
import { readdirSync, readFileSync, writeFileSync, mkdirSync } from 'node:fs'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'

// Иконки хранятся вектором, а в игру попадают текстурами: Minecraft не умеет
// SVG. Плотность 8x от размера в макете выбрана так, чтобы иконка 16 единиц
// оставалась чёткой при GUI Scale 4 на 4K, где ей достаётся 64 физических
// пикселя, и при этом уменьшение шло линейной фильтрацией без муара.
const SCALE = 8

const here = dirname(fileURLToPath(import.meta.url))
const source = resolve(here, '..', 'src/main/resources/assets/aspectvisuals/icons')
const target = resolve(here, '..', 'src/main/resources/assets/aspectvisuals/textures/icon')

mkdirSync(target, { recursive: true })

const files = readdirSync(source).filter((name) => name.endsWith('.svg'))
for (const file of files) {
  const svg = readFileSync(join(source, file), 'utf8')
  const resvg = new Resvg(svg, {
    fitTo: { mode: 'width', value: 16 * SCALE },
    background: 'rgba(0,0,0,0)',
  })
  const png = resvg.render().asPng()
  const name = file.replace(/\.svg$/, '.png')
  writeFileSync(join(target, name), png)
  console.log(`${file} -> textures/icon/${name} (${16 * SCALE}px)`)
}

console.log(`Готово: ${files.length} иконок`)
