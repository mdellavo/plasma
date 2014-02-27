uniform float uSize;
uniform mat4 uMatrix;

attribute vec3 aPosition;

void main() {
    gl_Position = uMatrix * vec4(aPosition, 1);
    gl_PointSize = uSize;
}
