#version 150

uniform sampler2D Sampler0;

uniform vec2 AspectTextParams;
uniform vec4 AspectClip;
uniform vec2 AspectScreen;

in vec2 texCoord;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    // В атласе лежит нормированное поле расстояния: 0.5 — контур глифа
    float field = texture(Sampler0, texCoord).r;
    float distance = (field - 0.5) * 2.0 * AspectTextParams.x + AspectTextParams.y;

    // Ширина перехода — ровно один физический пиксель на любом кегле
    float aa = max(fwidth(distance), 0.0001);
    float alpha = clamp(distance / aa + 0.5, 0.0, 1.0);

    if (AspectClip.z > 0.0 && AspectClip.w > 0.0) {
        vec2 halfSize = AspectClip.zw * 0.5;
        vec2 pixel = vec2(gl_FragCoord.x, AspectScreen.y - gl_FragCoord.y);
        vec2 point = abs(pixel - (AspectClip.xy + halfSize)) - halfSize;
        float clipDist = min(max(point.x, point.y), 0.0) + length(max(point, vec2(0.0)));
        float clipAa = max(fwidth(clipDist), 0.0001);
        alpha *= 1.0 - smoothstep(-clipAa, clipAa, clipDist);
    }

    if (alpha < 0.0005) {
        discard;
    }
    fragColor = vec4(vertexColor.rgb, vertexColor.a * alpha);
}
