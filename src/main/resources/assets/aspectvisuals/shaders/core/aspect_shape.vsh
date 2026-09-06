#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;

    // Координата фигуры едет отдельным атрибутом, а не выводится из Position:
    // Position приходит сюда уже умноженной на матрицу интерфейса, и виджет
    // HUD, поставленный на место сдвигом матрицы, давал бы смещение.
    localPos = UV0;
}
