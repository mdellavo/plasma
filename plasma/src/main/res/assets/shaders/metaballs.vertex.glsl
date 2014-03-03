uniform float uSize;
uniform mat4 uMatrix;

attribute vec3 aPosition;
attribute vec4 aColor;

varying vec4 vColor;

void main() {
    gl_Position = uMatrix * vec4(aPosition, 1);
    gl_PointSize = uSize;
    vColor = aColor;
}
