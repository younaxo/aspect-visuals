#version 150

in vec3 Position;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    vertexColor = Color;
    // Координаты фигуры доходят до фрагмента без округления,
    // поэтому сглаживание считается по реальной плотности буфера
    localPos = Position.xy;
}
