#version 150

in vec4 vertexColor;
in vec2 localPos;

uniform vec4 AspectRect;
uniform vec4 AspectRadius;
uniform vec4 AspectBorderColor;
uniform vec4 AspectGradient;
uniform vec4 AspectClip;
uniform vec2 AspectParams;
uniform vec2 AspectScreen;

out vec4 fragColor;

// Знаковое расстояние до скруглённого прямоугольника.
// Радиусы задаются по углам: x = левый верхний, y = правый верхний,
// z = правый нижний, w = левый нижний.
float roundedBoxSdf(vec2 point, vec2 halfSize, vec4 radius) {
    float corner = point.x > 0.0
        ? (point.y > 0.0 ? radius.z : radius.y)
        : (point.y > 0.0 ? radius.w : radius.x);
    corner = min(corner, min(halfSize.x, halfSize.y));

    vec2 q = abs(point) - halfSize + corner;
    return min(max(q.x, q.y), 0.0) + length(max(q, vec2(0.0))) - corner;
}

void main() {
    // Фигура считается в своей системе координат, которую принёс атрибут:
    // она не зависит ни от GUI Scale, ни от сдвига и масштаба виджета
    vec2 halfSize = AspectRect.zw * 0.5;
    vec2 point = localPos - (AspectRect.xy + halfSize);
    float dist = roundedBoxSdf(point, halfSize, AspectRadius);

    // Ширина сглаживания берётся из производной, поэтому она равна одному
    // физическому пикселю при любом разрешении, GUI Scale и масштабе виджета
    float aa = max(fwidth(dist), 0.0001);

    float border = AspectParams.x;
    float softness = AspectParams.y;

    float gradientT = clamp((localPos.y - AspectRect.y) / max(AspectRect.w, 0.0001), 0.0, 1.0);
    vec4 base = mix(vertexColor, AspectGradient, gradientT);

    vec4 result;
    if (softness > 0.0) {
        // Тень: то же поле расстояния с широким спадом вместо растрового ассета
        float shadow = 1.0 - smoothstep(-softness, softness, dist);
        result = vec4(base.rgb, base.a * shadow * shadow);
    } else {
        float outer = 1.0 - smoothstep(-aa, aa, dist);
        float inner = 1.0 - smoothstep(-aa, aa, dist + border);
        float ring = clamp(outer - inner, 0.0, 1.0);

        // Граница считается из той же фигуры, поэтому на углах нет стыков
        vec4 body = vec4(base.rgb, base.a * inner);
        vec4 edge = vec4(AspectBorderColor.rgb, AspectBorderColor.a * ring);

        float alpha = edge.a + body.a * (1.0 - edge.a);
        vec3 rgb = alpha > 0.0
            ? (edge.rgb * edge.a + body.rgb * body.a * (1.0 - edge.a)) / alpha
            : base.rgb;
        result = vec4(rgb, alpha);
    }

    // Обрезка тоже аналитическая: ножницы OpenGL режут по целым логическим
    // пикселям и съедают сглаживание по краю области
    if (AspectClip.z > 0.0 && AspectClip.w > 0.0) {
        // Обрезка задаётся в пикселях буфера: область принадлежит экрану,
        // а не фигуре, поэтому берётся из gl_FragCoord
        vec2 pixel = vec2(gl_FragCoord.x, AspectScreen.y - gl_FragCoord.y);
        vec2 clipHalf = AspectClip.zw * 0.5;
        float clipDist = roundedBoxSdf(pixel - (AspectClip.xy + clipHalf), clipHalf, vec4(0.0));
        float clipAa = max(fwidth(clipDist), 0.0001);
        result.a *= 1.0 - smoothstep(-clipAa, clipAa, clipDist);
    }

    if (result.a < 0.0005) {
        discard;
    }
    fragColor = result;
}
