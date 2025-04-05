package org.lostcityinterfaceeditor.baseCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Model {
        public String name;
        public int vertexCount;
        public int[] verticesX;
        public int[] verticesY;
        public int[] verticesZ;
        public int faceCount;
        public int[] faceIndicesA;
        public int[] faceIndicesB;
        public int[] faceIndicesC;
        public int[] faceColorA;
        private int[] faceColorB;
        private int[] faceColorC;
        public int[] faceInfos;
        public int[] facePriorities;
        public int[] faceAlphas;
        public int[] faceColors;
        private int modelPriority;
        public int texturedFaceCount;
        public int[] texturePCoordinate;
        public int[] textureMCoordinate;
        public int[] textureNCoordinate;
        public int minX;
        public int maxX;
        public int maxZ;
        public int minZ;
        public int radius;
        public int maxY;
        public int minY;
        private int maxDepth;
        private int minDepth;
        public int objRaise;
        public int[] vertexLabels;
        public int[] faceLabels;
        public int[][] labelVertices;
        public int[][] labelFaces;
        public VertexNormal[] vertexNormal;
        public VertexNormal[] vertexNormalOriginal;
        public int baseX = 0;
        public int baseY = 0;
        public int baseZ = 0;
        public static final int[] pickedBitsets = new int[1000];
        public int[] faceTextures;
        public int[] textureCoords;
        public static int[] vertexScreenX = new int[4096];
        public static int[] vertexScreenY = new int[4096];
        public static int[] vertexScreenZ = new int[4096];
        public static int[] vertexViewSpaceX = new int[4096];
        public static int[] vertexViewSpaceY = new int[4096];
        public static int[] vertexViewSpaceZ = new int[4096];
        public static int[] tmpDepthFaceCount = new int[1500];
        public static boolean[] faceClippedX = new boolean[4096];
        public static boolean[] faceNearClipped = new boolean[4096];
        public static int[][] tmpDepthFaces = new int[1500][512];
        public static int[] tmpPriorityFaceCount = new int[12];
        public static int[][] tmpPriorityFaces = new int[12][2000];
        public static int[] tmpPriority10FaceDepth = new int[2000];
        public static int[] tmpPriority11FaceDepth = new int[2000];
        public static int[] tmpPriorityDepthSum = new int[12];
        public static final int[] clippedX = new int[10];
        public static final int[] clippedY = new int[10];
        public static final int[] clippedColor = new int[10];
        public static int[] sinTable = Pix3D.sinTable;
        public static int[] cosTable = Pix3D.cosTable;
        public static int[] colourTable = Pix3D.colourTable;
        public static int[] divTable2 = Pix3D.divTable2;

    public Model() {

    }

    public static final class VertexNormal {
        public int x;
        public int y;
        public int z;
        public int w;
    }

    private static class OB2Packet {
        private final byte[] data;
        private int position;

        public OB2Packet(byte[] data) {
            this.data = data;
            this.position = 0;
        }

        public OB2Packet(int[] data) {
            this.data = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                this.data[i] = (byte) data[i];
            }
            this.position = 0;
        }

        public byte[] getData() {
            return data;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getPosition() {
            return position;
        }

        public int gSmart() {
            int peekByte = this.getData()[this.getPosition()] & 0xFF;

            if (peekByte < 128) {
                return this.getUnsignedByte() - 64;
            } else {
                return this.getUnsignedShort() - 49152;
            }
        }

        public int getUnsignedByte() {
            return data[position++] & 0xFF;
        }

        public int getUnsignedShort() {
            return ((data[position++] & 0xFF) << 8) | (data[position++] & 0xFF);
        }
    }

    private static Model parseOB2(OB2Packet data) {
        Model model = new Model();

        data.setPosition(data.getData().length - 18);

        int vertexCount = data.getUnsignedShort();
        int faceCount = data.getUnsignedShort();
        int texturedFaceCount = data.getUnsignedByte();
        boolean hasInfo = data.getUnsignedByte() == 1;
        int hasPriorities = data.getUnsignedByte();
        boolean hasAlpha = data.getUnsignedByte() == 1;
        boolean hasFaceLabels = data.getUnsignedByte() == 1;
        boolean hasVertexLabels = data.getUnsignedByte() == 1;
        int vertexXLength = data.getUnsignedShort();
        int vertexYLength = data.getUnsignedShort();
        int vertexZLength = data.getUnsignedShort();
        int faceVertexLength = data.getUnsignedShort();

        model.faceCount = faceCount;
        model.faceIndicesA = new int[faceCount];
        model.faceIndicesB = new int[faceCount];
        model.faceIndicesC = new int[faceCount];
        model.faceColors = new int[faceCount];

        model.vertexCount = vertexCount;
        model.verticesX = new int[vertexCount];
        model.verticesY = new int[vertexCount];
        model.verticesZ = new int[vertexCount];
        data.setPosition(0);

        model.texturedFaceCount = texturedFaceCount;
        if(texturedFaceCount != 0) {
            model.textureMCoordinate = new int[texturedFaceCount];
            model.texturePCoordinate = new int[texturedFaceCount];
            model.textureNCoordinate = new int[texturedFaceCount];
        }

        int[] vertexFlags = new int[vertexCount];
        for (int i = 0; i < vertexCount; i++) {
            vertexFlags[i] = data.getUnsignedByte();
        }


        int[] faceIndices = new int[faceCount];
        for (int i = 0; i < faceCount; i++) {
            faceIndices[i] = data.getUnsignedByte();
        }


        int[] priorities = null;
        if (hasPriorities == 255) {
            priorities = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                priorities[i] = data.getUnsignedByte();
            }
            model.facePriorities = priorities;
        }

        int[] faceLabels = null;
        if (hasFaceLabels) {
            faceLabels = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                faceLabels[i] = data.getUnsignedByte();
            }
            model.faceLabels = faceLabels;
        }

        int[] faceInfo = null;
        int[] textureCoords = null;
        int[] faceTextures = null;
        if (hasInfo) {
            faceInfo = new int[faceCount];
            faceTextures = new int[faceCount];
            textureCoords = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                faceInfo[i] = data.getUnsignedByte();
                if ((faceInfo[i] & 0x2) == 2) {
                    textureCoords[i] = faceInfo[i] >> 2;
                    faceTextures[i] = model.faceColors[i];
                } else {
                    textureCoords[i] = -1;
                    faceTextures[i] = -1;
                }
            }
            model.faceInfos = faceInfo;
            model.faceTextures = faceTextures;
            model.textureCoords = textureCoords;
        }

        int[] vertexLabels = null;
        if (hasVertexLabels) {
            vertexLabels = new int[vertexCount];
            for (int i = 0; i < vertexCount; i++) {
                vertexLabels[i] = data.getUnsignedByte();
            }
            model.vertexLabels = vertexLabels;
        }

        int[] alphaValues = null;
        if (hasAlpha) {
            alphaValues = new int[faceCount];
            for (int i = 0; i < faceCount; i++) {
                alphaValues[i] = data.getUnsignedByte();
            }
            model.faceAlphas = alphaValues;
        }

        int[] faceVertexData = new int[faceVertexLength];
        for (int i = 0; i < faceVertexLength; i++) {
            faceVertexData[i] = data.getUnsignedByte();
        }

        int[] faceTypeData = new int[faceCount * 2];
        for (int i = 0; i < faceCount * 2; i++) {
            faceTypeData[i] = data.getUnsignedByte();
        }

        int[] texturedFaceData = new int[texturedFaceCount * 6];
        for (int i = 0; i < texturedFaceCount * 6; i++) {
            texturedFaceData[i] = data.getUnsignedByte();
        }
        int[] vertexXData = new int[vertexXLength];
        for (int i = 0; i < vertexXLength; i++) {
            vertexXData[i] = data.getUnsignedByte();
        }

        int[] vertexYData = new int[vertexYLength];
        for (int i = 0; i < vertexYLength; i++) {
            vertexYData[i] = data.getUnsignedByte();
        }

        int[] vertexZData = new int[vertexZLength];
        for (int i = 0; i < vertexZLength; i++) {
            vertexZData[i] = data.getUnsignedByte();
        }

        processVertices(model, vertexXData, vertexYData, vertexZData, vertexFlags);

        processFaces(model, faceVertexData, faceIndices);

        processColors(model, faceTypeData);

        processTextures(model, texturedFaceData);
        return model;
    }

    private static void processColors(Model model, int[] faceTypeData) {
        OB2Packet colorData = new OB2Packet(faceTypeData);
        for (int f = 0;f < model.faceCount; f++) {
            int color = colorData.getUnsignedShort();
            model.faceColors[f] = color;
        }
    }

    private static void processVertices(Model model, int[] xData, int[] yData, int[] zData, int[] vertexFlags) {

        OB2Packet dataX = new OB2Packet(xData);
        OB2Packet dataY = new OB2Packet(yData);
        OB2Packet dataZ = new OB2Packet(zData);

        int dx = 0;
        int dy = 0;
        int dz = 0;
        for (int v = 0; v < model.vertexCount; v++) {
            int flags = vertexFlags[v];

            int a = 0;
            if ((flags & 1) != 0) {
                a = dataX.gSmart();
            }
            int b = 0;
            if ((flags & 2) != 0) {
                b = dataY.gSmart();
            }
            int c = 0;
            if ((flags & 4) != 0) {
                c = dataZ.gSmart();
            }

            int x = dx + a;
            int y = dy + b;
            int z = dz + c;

            dx = x;
            dy = y;
            dz = z;

            model.verticesX[v] = x;
            model.verticesY[v] = y;
            model.verticesZ[v] = z;
        }
    }

    private static void processFaces(Model model, int[] faceTypeData, int[] faceIndices) {
        OB2Packet vertexData = new OB2Packet(faceTypeData);
        OB2Packet orientationData = new OB2Packet(faceIndices);
        int a = 0;
        int b = 0;
        int c = 0;
        int last = 0;
        for (int f = 0;f < model.faceCount; f++) {
            int orientation = orientationData.getUnsignedByte();
            if (orientation == 1) {
                a = vertexData.gSmart() + last;
                last = a;
                b = vertexData.gSmart() + last;
                last = b;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 2) {
                b = c;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 3) {
                a = c;
                c = vertexData.gSmart() + last;
                last = c;
            } else if (orientation == 4) {
                int tmp = a;
                a = b;
                b = tmp;
                c = vertexData.gSmart() + last;
                last = c;
            }
            model.faceIndicesA[f] = a;
            model.faceIndicesB[f] = b;
            model.faceIndicesC[f] = c;
        }
    }

    private static void processTextures(Model model, int[] texturedFaceData) {
        OB2Packet textureData = new OB2Packet(texturedFaceData);

        for (int i = 0; i < model.texturedFaceCount; i++) {
            model.texturePCoordinate[i] = textureData.getUnsignedShort();
            model.textureMCoordinate[i] = textureData.getUnsignedShort();
            model.textureNCoordinate[i] = textureData.getUnsignedShort();
        }
    }

    public static Model convert(Path filePath) throws IOException {
        byte[] fileData;
        try {
            fileData = Files.readAllBytes(filePath); // Read bytes directly from the file
        } catch (IOException e) {
            System.err.println("Error loading image from file: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw the exception to signal failure up the call stack
        }

        OB2Packet packet = new OB2Packet(fileData);
        return parseOB2(packet);
    }

    public void calculateNormals( int lightAmbient,  int lightAttenuation,  int lightSrcX,  int lightSrcY,  int lightSrcZ,  boolean applyLighting) {
        int lightMagnitude = (int) Math.sqrt(lightSrcX * lightSrcX + lightSrcY * lightSrcY + lightSrcZ * lightSrcZ);
        int attenuation = lightAttenuation * lightMagnitude >> 8;

        if (this.faceColorA == null) {
            this.faceColorA = new int[this.faceCount];
            this.faceColorB = new int[this.faceCount];
            this.faceColorC = new int[this.faceCount];
        }

        if (this.vertexNormal == null) {
            this.vertexNormal = new VertexNormal[this.vertexCount];
            for (int v = 0; v < this.vertexCount; v++) {
                this.vertexNormal[v] = new VertexNormal();
            }
        }

        for (int f = 0; f < this.faceCount; f++) {
            int a = this.faceIndicesA[f];
            int b = this.faceIndicesB[f];
            int c = this.faceIndicesC[f];

            int dxAB = this.verticesX[b] - this.verticesX[a];
            int dyAB = this.verticesY[b] - this.verticesY[a];
            int dzAB = this.verticesZ[b] - this.verticesZ[a];

            int dxAC = this.verticesX[c] - this.verticesX[a];
            int dyAC = this.verticesY[c] - this.verticesY[a];
            int dzAC = this.verticesZ[c] - this.verticesZ[a];

            int nx = dyAB * dzAC - dyAC * dzAB;
            int ny = dzAB * dxAC - dzAC * dxAB;
            int nz;
            for (nz = dxAB * dyAC - dxAC * dyAB; nx > 8192 || ny > 8192 || nz > 8192 || nx < -8192 || ny < -8192 || nz < -8192; nz >>= 0x1) {
                nx >>= 0x1;
                ny >>= 0x1;
            }

            int length = (int) Math.sqrt(nx * nx + ny * ny + nz * nz);
            if (length <= 0) {
                length = 1;
            }

            nx = nx * 256 / length;
            ny = ny * 256 / length;
            nz = nz * 256 / length;

            if (this.faceInfos == null || (this.faceInfos[f] & 0x1) == 0) {
                VertexNormal n = this.vertexNormal[a];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;

                n = this.vertexNormal[b];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;

                n = this.vertexNormal[c];
                n.x += nx;
                n.y += ny;
                n.z += nz;
                n.w++;
            } else {
                int lightness = lightAmbient + (lightSrcX * nx + lightSrcY * ny + lightSrcZ * nz) / (attenuation + attenuation / 2);
                this.faceColorA[f] = mulColorLightness(this.faceColors[f], lightness, this.faceInfos[f]);
            }
        }

        if (applyLighting) {
            this.applyLighting(lightAmbient, attenuation, lightSrcX, lightSrcY, lightSrcZ);
        } else {
            this.vertexNormalOriginal = new VertexNormal[this.vertexCount];
            for (int v = 0; v < this.vertexCount; v++) {
                VertexNormal normal = this.vertexNormal[v];
                VertexNormal copy = this.vertexNormalOriginal[v] = new VertexNormal();
                copy.x = normal.x;
                copy.y = normal.y;
                copy.z = normal.z;
                copy.w = normal.w;
            }
        }

        if (applyLighting) {
            this.calculateBoundsCylinder();
        } else {
            this.calculateBoundsAABB();
        }
    }

    public void calculateBoundsCylinder() {
        this.maxY = 0;
        this.radius = 0;
        this.minY = 0;

        for ( int i = 0; i < this.vertexCount; i++) {
            int x = this.verticesX[i];
            int y = this.verticesY[i];
            int z = this.verticesZ[i];

            if (-y > this.maxY) {
                this.maxY = -y;
            }
            if (y > this.minY) {
                this.minY = y;
            }

            int radiusSqr = x * x + z * z;
            if (radiusSqr > this.radius) {
                this.radius = radiusSqr;
            }
        }

        this.radius = (int) (Math.sqrt(this.radius) + 0.99D);
        this.minDepth = (int) (Math.sqrt(this.radius * this.radius + this.maxY * this.maxY) + 0.99D);
        this.maxDepth = this.minDepth + (int) (Math.sqrt(this.radius * this.radius + this.minY * this.minY) + 0.99D);
    }

    private void calculateBoundsAABB() {
        this.maxY = 0;
        this.radius = 0;
        this.minY = 0;
        this.minX = 999999;
        this.maxX = -999999;
        this.maxZ = -99999;
        this.minZ = 99999;

        for ( int v = 0; v < this.vertexCount; v++) {
            int x = this.verticesX[v];
            int y = this.verticesY[v];
            int z = this.verticesZ[v];

            if (x < this.minX) {
                this.minX = x;
            }
            if (x > this.maxX) {
                this.maxX = x;
            }

            if (z < this.minZ) {
                this.minZ = z;
            }
            if (z > this.maxZ) {
                this.maxZ = z;
            }

            if (-y > this.maxY) {
                this.maxY = -y;
            }
            if (y > this.minY) {
                this.minY = y;
            }

            int radiusSqr = x * x + z * z;
            if (radiusSqr > this.radius) {
                this.radius = radiusSqr;
            }
        }

        this.radius = (int) Math.sqrt(this.radius);
        this.minDepth = (int) Math.sqrt(this.radius * this.radius + this.maxY * this.maxY);
        this.maxDepth = this.minDepth + (int) Math.sqrt(this.radius * this.radius + this.minY * this.minY);
    }


    public void applyLighting( int lightAmbient,  int lightAttenuation,  int lightSrcX,  int lightSrcY,  int lightSrcZ) {
        for ( int f = 0; f < this.faceCount; f++) {
            int a = this.faceIndicesA[f];
            int b = this.faceIndicesB[f];
            int c = this.faceIndicesC[f];

            if (this.faceInfos == null) {
                int color;
                if (this.faceColors == null || this.faceColors.length <= f) {
                    color = 0xFFFF00FF;
                } else {
                    color = this.faceColors[f];
                }

                VertexNormal n = this.vertexNormal[a];
                int lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorA[f] = mulColorLightness(color, lightness, 0);

                n = this.vertexNormal[b];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorB[f] = mulColorLightness(color, lightness, 0);

                n = this.vertexNormal[c];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorC[f] = mulColorLightness(color, lightness, 0);
            } else if ((this.faceInfos[f] & 0x1) == 0) {
                int color = this.faceColors[f];
                int info = this.faceInfos[f];

                VertexNormal n = this.vertexNormal[a];
                int lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorA[f] = mulColorLightness(color, lightness, info);

                n = this.vertexNormal[b];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorB[f] = mulColorLightness(color, lightness, info);

                n = this.vertexNormal[c];
                lightness = lightAmbient + (lightSrcX * n.x + lightSrcY * n.y + lightSrcZ * n.z) / (lightAttenuation * n.w);
                this.faceColorC[f] = mulColorLightness(color, lightness, info);
            }
        }

        this.vertexNormal = null;
        this.vertexNormalOriginal = null;
        this.vertexLabels = null;
        this.faceLabels = null;

        if (this.faceInfos != null) {
            for (int f = 0; f < this.faceCount; f++) {
                if ((this.faceInfos[f] & 0x2) == 2) {
                    return;
                }
            }
        }

        this.faceColors = null;
    }

    public static int mulColorLightness( int hsl,  int scalar,  int faceInfo) {
        if ((faceInfo & 0x2) == 2) {
            if (scalar < 0) {
                scalar = 0;
            } else if (scalar > 127) {
                scalar = 127;
            }
            return 127 - scalar;
        }
        scalar = scalar * (hsl & 0x7F) >> 7;
        if (scalar < 2) {
            scalar = 2;
        } else if (scalar > 126) {
            scalar = 126;
        }
        return (hsl & 0xFF80) + scalar;
    }

    public void drawSimple(int pitch, int yaw, int roll, int eyePitch, int eyeX, int eyeY, int eyeZ) {
        int centerX = Pix3D.centerW3D;
        int centerY = Pix3D.centerH3D;
        int sinPitch = sinTable[pitch];
        int cosPitch = cosTable[pitch];
        int sinYaw = sinTable[yaw];
        int cosYaw = cosTable[yaw];
        int sinRoll = sinTable[roll];
        int cosRoll = cosTable[roll];
        int sinEyePitch = sinTable[eyePitch];
        int cosEyePitch = cosTable[eyePitch];
        int midZ = eyeY * sinEyePitch + eyeZ * cosEyePitch >> 16;

        for (int v = 0; v < this.vertexCount; v++) {
            int x = this.verticesX[v];
            int y = this.verticesY[v];
            int z = this.verticesZ[v];

            int temp;
            if (roll != 0) {
                temp = y * sinRoll + x * cosRoll >> 16;
                y = y * cosRoll - x * sinRoll >> 16;
                x = temp;
            }

            if (pitch != 0) {
                temp = y * cosPitch - z * sinPitch >> 16;
                z = y * sinPitch + z * cosPitch >> 16;
                y = temp;
            }

            if (yaw != 0) {
                temp = z * sinYaw + x * cosYaw >> 16;
                z = z * cosYaw - x * sinYaw >> 16;
                x = temp;
            }

            x += eyeX;
            y += eyeY;
            z += eyeZ;

            temp = y * cosEyePitch - z * sinEyePitch >> 16;
            z = y * sinEyePitch + z * cosEyePitch >> 16;

            vertexScreenZ[v] = z - midZ;
            vertexScreenX[v] = centerX + (x << 9) / z;
            vertexScreenY[v] = centerY + (temp << 9) / z;

            if (this.texturedFaceCount > 0) {
                vertexViewSpaceX[v] = x;
                vertexViewSpaceY[v] = temp;
                vertexViewSpaceZ[v] = z;
            }
        }

        try {
            this.draw(false, false, 0);
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }

    public void draw(boolean clipped, boolean picking, int bitset) {

        for (int depth = 0; depth < this.maxDepth; depth++) {
            if (Model.tmpDepthFaceCount != null) {
                Model.tmpDepthFaceCount[depth] = 0;
            }
        }

        for (int f = 0; f < this.faceCount; f++) {
            if (this.faceInfos != null && this.faceInfos[f] == -1) {
                continue;
            }

            if (Model.vertexScreenX != null && Model.vertexScreenY != null && Model.vertexScreenZ != null
                    && Model.tmpDepthFaces != null && Model.tmpDepthFaceCount != null) {
                int a = this.faceIndicesA[f];
                int b = this.faceIndicesB[f];
                int c = this.faceIndicesC[f];
                int xA = Model.vertexScreenX[a];
                int xB = Model.vertexScreenX[b];
                int xC = Model.vertexScreenX[c];
                int yA = Model.vertexScreenY[a];
                int yB = Model.vertexScreenY[b];
                int yC = Model.vertexScreenY[c];
                int zA = Model.vertexScreenZ[a];
                int zB = Model.vertexScreenZ[b];
                int zC = Model.vertexScreenZ[c];

                if (clipped && (xA == -5000 || xB == -5000 || xC == -5000)) {
                    if (Model.faceNearClipped != null) {
                        Model.faceNearClipped[f] = true;
                    }
                    if (Model.tmpDepthFaces != null && Model.tmpDepthFaceCount != null) {
                        int depthAverage = ((zA + zB + zC) / 3) + this.minDepth;
                        Model.tmpDepthFaces[depthAverage][Model.tmpDepthFaceCount[depthAverage]++] = f;
                    }
                } else {
                    int dxAB = xA - xB;
                    int dyAB = yA - yB;
                    int dxCB = xC - xB;
                    int dyCB = yC - yB;

                    if (dxAB * dyCB - dyAB * dxCB <= 0) {
                        continue;
                    }

                    if (Model.faceNearClipped != null) {
                        Model.faceNearClipped[f] = false;
                    }

                    if (Model.faceClippedX != null) {
                        Model.faceClippedX[f] = xA < 0 || xB < 0 || xC < 0 ||
                                xA > Pix2D.safeWidth || xB > Pix2D.safeWidth || xC > Pix2D.safeWidth;
                    }

                    if (Model.tmpDepthFaces != null && Model.tmpDepthFaceCount != null) {
                        int depthAverage = ((zA + zB + zC) / 3) + this.minDepth;
                        Model.tmpDepthFaces[depthAverage][Model.tmpDepthFaceCount[depthAverage]++] = f;
                    }
                }
            }
        }

        if (this.facePriorities == null && Model.tmpDepthFaceCount != null) {
            for (int depth = this.maxDepth - 1; depth >= 0; depth--) {
                int count = Model.tmpDepthFaceCount[depth];
                if (count <= 0) {
                    continue;
                }

                if (Model.tmpDepthFaces != null) {
                    int[] faces = Model.tmpDepthFaces[depth];
                    for (int f = 0; f < count; f++) {
                        try {
                            this.drawFace(faces[f]);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return;
        }

        for (int priority = 0; priority < 12; priority++) {
            if (Model.tmpPriorityFaceCount != null && Model.tmpPriorityDepthSum != null) {
                Model.tmpPriorityFaceCount[priority] = 0;
                Model.tmpPriorityDepthSum[priority] = 0;
            }
        }

        if (Model.tmpDepthFaceCount != null) {
            for (int depth = this.maxDepth - 1; depth >= 0; depth--) {
                int faceCount = Model.tmpDepthFaceCount[depth];
                if (faceCount > 0 && Model.tmpDepthFaces != null) {
                    int[] faces = Model.tmpDepthFaces[depth];
                    for (int i = 0; i < faceCount; i++) {
                        if (this.facePriorities != null && Model.tmpPriorityFaceCount != null && Model.tmpPriorityFaces != null) {
                            int priorityDepth = faces[i];
                            int priorityFace = this.facePriorities[priorityDepth];
                            int priorityFaceCount = Model.tmpPriorityFaceCount[priorityFace]++;
                            Model.tmpPriorityFaces[priorityFace][priorityFaceCount] = priorityDepth;

                            if (priorityFace < 10 && Model.tmpPriorityDepthSum != null) {
                                Model.tmpPriorityDepthSum[priorityFace] += depth;
                            } else if (priorityFace == 10 && Model.tmpPriority10FaceDepth != null) {
                                Model.tmpPriority10FaceDepth[priorityFaceCount] = depth;
                            } else if (Model.tmpPriority11FaceDepth != null) {
                                Model.tmpPriority11FaceDepth[priorityFaceCount] = depth;
                            }
                        }
                    }
                }
            }
        }

        int averagePriorityDepthSum1_2 = 0;
        if (Model.tmpPriorityFaceCount != null && Model.tmpPriorityDepthSum != null &&
                (Model.tmpPriorityFaceCount[1] > 0 || Model.tmpPriorityFaceCount[2] > 0)) {
            averagePriorityDepthSum1_2 = (Model.tmpPriorityDepthSum[1] + Model.tmpPriorityDepthSum[2]) /
                    (Model.tmpPriorityFaceCount[1] + Model.tmpPriorityFaceCount[2]);
        }

        int averagePriorityDepthSum3_4 = 0;
        if (Model.tmpPriorityFaceCount != null && Model.tmpPriorityDepthSum != null &&
                (Model.tmpPriorityFaceCount[3] > 0 || Model.tmpPriorityFaceCount[4] > 0)) {
            averagePriorityDepthSum3_4 = (Model.tmpPriorityDepthSum[3] + Model.tmpPriorityDepthSum[4]) /
                    (Model.tmpPriorityFaceCount[3] + Model.tmpPriorityFaceCount[4]);
        }

        int averagePriorityDepthSum6_8 = 0;
        if (Model.tmpPriorityFaceCount != null && Model.tmpPriorityDepthSum != null &&
                (Model.tmpPriorityFaceCount[6] > 0 || Model.tmpPriorityFaceCount[8] > 0)) {
            averagePriorityDepthSum6_8 = (Model.tmpPriorityDepthSum[6] + Model.tmpPriorityDepthSum[8]) /
                    (Model.tmpPriorityFaceCount[6] + Model.tmpPriorityFaceCount[8]);
        }

        if (Model.tmpPriorityFaceCount != null && Model.tmpPriorityFaces != null) {
            int priorityFace = 0;
            int priorityFaceCount = Model.tmpPriorityFaceCount[10];
            int[] priorityFaces = Model.tmpPriorityFaces[10];
            int[] priorityFaceDepths = Model.tmpPriority10FaceDepth;

            if (priorityFace == priorityFaceCount) {
                priorityFace = 0;
                priorityFaceCount = Model.tmpPriorityFaceCount[11];
                priorityFaces = Model.tmpPriorityFaces[11];
                priorityFaceDepths = Model.tmpPriority11FaceDepth;
            }

            int priorityDepth;
            if (priorityFace < priorityFaceCount && priorityFaceDepths != null) {
                priorityDepth = priorityFaceDepths[priorityFace];
            } else {
                priorityDepth = -1000;
            }

            for (int priority = 0; priority < 10; priority++) {
                while (priority == 0 && priorityDepth > averagePriorityDepthSum1_2) {
                    try {
                        this.drawFace(priorityFaces[priorityFace++]);
                        if (priorityFace == priorityFaceCount && priorityFaces != Model.tmpPriorityFaces[11]) {
                            priorityFace = 0;
                            priorityFaceCount = Model.tmpPriorityFaceCount[11];
                            priorityFaces = Model.tmpPriorityFaces[11];
                            priorityFaceDepths = Model.tmpPriority11FaceDepth;
                        }
                        if (priorityFace < priorityFaceCount && priorityFaceDepths != null) {
                            priorityDepth = priorityFaceDepths[priorityFace];
                        } else {
                            priorityDepth = -1000;
                        }
                    } catch (Exception e) {
                    }
                }

                while (priority == 3 && priorityDepth > averagePriorityDepthSum3_4) {
                    try {
                        this.drawFace(priorityFaces[priorityFace++]);
                        if (priorityFace == priorityFaceCount && priorityFaces != Model.tmpPriorityFaces[11]) {
                            priorityFace = 0;
                            priorityFaceCount = Model.tmpPriorityFaceCount[11];
                            priorityFaces = Model.tmpPriorityFaces[11];
                            priorityFaceDepths = Model.tmpPriority11FaceDepth;
                        }
                        if (priorityFace < priorityFaceCount && priorityFaceDepths != null) {
                            priorityDepth = priorityFaceDepths[priorityFace];
                        } else {
                            priorityDepth = -1000;
                        }
                    } catch (Exception e) {
                    }
                }

                while (priority == 5 && priorityDepth > averagePriorityDepthSum6_8) {
                    try {
                        this.drawFace(priorityFaces[priorityFace++]);
                        if (priorityFace == priorityFaceCount && priorityFaces != Model.tmpPriorityFaces[11]) {
                            priorityFace = 0;
                            priorityFaceCount = Model.tmpPriorityFaceCount[11];
                            priorityFaces = Model.tmpPriorityFaces[11];
                            priorityFaceDepths = Model.tmpPriority11FaceDepth;
                        }
                        if (priorityFace < priorityFaceCount && priorityFaceDepths != null) {
                            priorityDepth = priorityFaceDepths[priorityFace];
                        } else {
                            priorityDepth = -1000;
                        }
                    } catch (Exception e) {
                    }
                }

                int count = Model.tmpPriorityFaceCount[priority];
                int[] faces = Model.tmpPriorityFaces[priority];
                for (int i = 0; i < count; i++) {
                    try {
                        this.drawFace(faces[i]);
                    } catch (Exception e) {
                    }
                }
            }

            while (priorityDepth != -1000) {
                try {
                    this.drawFace(priorityFaces[priorityFace++]);
                    if (priorityFace == priorityFaceCount && priorityFaces != Model.tmpPriorityFaces[11]) {
                        priorityFace = 0;
                        priorityFaces = Model.tmpPriorityFaces[11];
                        priorityFaceCount = Model.tmpPriorityFaceCount[11];
                        priorityFaceDepths = Model.tmpPriority11FaceDepth;
                    }
                    if (priorityFace < priorityFaceCount && priorityFaceDepths != null) {
                        priorityDepth = priorityFaceDepths[priorityFace];
                    } else {
                        priorityDepth = -1000;
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    private void drawFace(int face) {
        if (faceNearClipped[face]) {
            this.drawNearClippedFace(face);
            return;
        }

        int a = this.faceIndicesA[face];
        int b = this.faceIndicesB[face];
        int c = this.faceIndicesC[face];

        Pix3D.hclip = faceClippedX[face];

        if (this.faceAlphas == null) {
            Pix3D.trans = 0;
        } else {
            Pix3D.trans = this.faceAlphas[face];
        }
        int type;
        if (this.faceInfos == null) {
            type = 0;
        } else {
            type = this.faceInfos[face] & 0x3;
        }

        if (type == 0) {
            Pix3D.gouraudTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorB[face], this.faceColorC[face]);
        } else if (type == 1) {
            Pix3D.flatTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], colourTable[this.faceColorA[face]]);
        } else if (type == 2) {
            int texturedFace = this.faceInfos[face] >> 2;
            int tA = this.texturePCoordinate[texturedFace];
            int tB = this.textureMCoordinate[texturedFace];
            int tC = this.textureNCoordinate[texturedFace];
            Pix3D.textureTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorB[face], this.faceColorC[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
        } else if (type == 3) {
            int texturedFace = this.faceInfos[face] >> 2;
            int tA = this.texturePCoordinate[texturedFace];
            int tB = this.textureMCoordinate[texturedFace];
            int tC = this.textureNCoordinate[texturedFace];
            Pix3D.textureTriangle(vertexScreenX[a], vertexScreenX[b], vertexScreenX[c], vertexScreenY[a], vertexScreenY[b], vertexScreenY[c], this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
        }
    }

    private void drawNearClippedFace(int face) {
        int centerX = Pix3D.centerW3D;
        int centerY = Pix3D.centerH3D;
        int elements = 0;

        int a = this.faceIndicesA[face];
        int b = this.faceIndicesB[face];
        int c = this.faceIndicesC[face];

        int zA = vertexViewSpaceZ[a];
        int zB = vertexViewSpaceZ[b];
        int zC = vertexViewSpaceZ[c];

        if (zA >= 50) {
            clippedX[elements] = vertexScreenX[a];
            clippedY[elements] = vertexScreenY[a];
            clippedColor[elements++] = this.faceColorA[face];
        } else {
            int xA = vertexViewSpaceX[a];
            int yA = vertexViewSpaceY[a];
            int colorA = this.faceColorA[face];

            if (zC >= 50) {
                int scalar = (50 - zA) * divTable2[zC - zA];
                clippedX[elements] = centerX + (xA + ((vertexViewSpaceX[c] - xA) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yA + ((vertexViewSpaceY[c] - yA) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorA + ((this.faceColorC[face] - colorA) * scalar >> 16);
            }

            if (zB >= 50) {
                int scalar = (50 - zA) * divTable2[zB - zA];
                clippedX[elements] = centerX + (xA + ((vertexViewSpaceX[b] - xA) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yA + ((vertexViewSpaceY[b] - yA) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorA + ((this.faceColorB[face] - colorA) * scalar >> 16);
            }
        }

        if (zB >= 50) {
            clippedX[elements] = vertexScreenX[b];
            clippedY[elements] = vertexScreenY[b];
            clippedColor[elements++] = this.faceColorB[face];
        } else {
            int xB = vertexViewSpaceX[b];
            int yB = vertexViewSpaceY[b];
            int colorB = this.faceColorB[face];

            if (zA >= 50) {
                int scalar = (50 - zB) * divTable2[zA - zB];
                clippedX[elements] = centerX + (xB + ((vertexViewSpaceX[a] - xB) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yB + ((vertexViewSpaceY[a] - yB) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorB + ((this.faceColorA[face] - colorB) * scalar >> 16);
            }

            if (zC >= 50) {
                int scalar = (50 - zB) * divTable2[zC - zB];
                clippedX[elements] = centerX + (xB + ((vertexViewSpaceX[c] - xB) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yB + ((vertexViewSpaceY[c] - yB) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorB + ((this.faceColorC[face] - colorB) * scalar >> 16);
            }
        }

        if (zC >= 50) {
            clippedX[elements] = vertexScreenX[c];
            clippedY[elements] = vertexScreenY[c];
            clippedColor[elements++] = this.faceColorC[face];
        } else {
            int xC = vertexViewSpaceX[c];
            int yC = vertexViewSpaceY[c];
            int colorC = this.faceColorC[face];

            if (zB >= 50) {
                int scalar = (50 - zC) * divTable2[zB - zC];
                clippedX[elements] = centerX + (xC + ((vertexViewSpaceX[b] - xC) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yC + ((vertexViewSpaceY[b] - yC) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorC + ((this.faceColorB[face] - colorC) * scalar >> 16);
            }

            if (zA >= 50) {
                int scalar = (50 - zC) * divTable2[zA - zC];
                clippedX[elements] = centerX + (xC + ((vertexViewSpaceX[a] - xC) * scalar >> 16) << 9) / 50;
                clippedY[elements] = centerY + (yC + ((vertexViewSpaceY[a] - yC) * scalar >> 16) << 9) / 50;
                clippedColor[elements++] = colorC + ((this.faceColorA[face] - colorC) * scalar >> 16);
            }
        }

        int x0 = clippedX[0];
        int x1 = clippedX[1];
        int x2 = clippedX[2];
        int y0 = clippedY[0];
        int y1 = clippedY[1];
        int y2 = clippedY[2];

        if ((x0 - x1) * (y2 - y1) - (y0 - y1) * (x2 - x1) <= 0) {
            return;
        }

        Pix3D.hclip = false;

        if (elements == 3) {
            if (x0 < 0 || x1 < 0 || x2 < 0 || x0 > Pix2D.safeWidth || x1 > Pix2D.safeWidth || x2 > Pix2D.safeWidth) {
                Pix3D.hclip = true;
            }

            int type;
            if (this.faceInfos == null) {
                type = 0;
            } else {
                type = this.faceInfos[face] & 0x3;
            }

            if (type == 0) {
                Pix3D.gouraudTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2]);
            } else if (type == 1) {
                Pix3D.flatTriangle(x0, x1, x2, y0, y1, y2, colourTable[this.faceColorA[face]]);
            } else if (type == 2) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            } else if (type == 3) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            }
        } else if (elements == 4) {
            if (x0 < 0 || x1 < 0 || x2 < 0 || x0 > Pix2D.safeWidth || x1 > Pix2D.safeWidth || x2 > Pix2D.safeWidth || clippedX[3] < 0 || clippedX[3] > Pix2D.safeWidth) {
                Pix3D.hclip = true;
            }

            int type;
            if (this.faceInfos == null) {
                type = 0;
            } else {
                type = this.faceInfos[face] & 0x3;
            }

            if (type == 0) {
                Pix3D.gouraudTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2]);
                Pix3D.gouraudTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], clippedColor[0], clippedColor[2], clippedColor[3]);
            } else if (type == 1) {
                int colorA = colourTable[this.faceColorA[face]];
                Pix3D.flatTriangle(x0, x1, x2, y0, y1, y2, colorA);
                Pix3D.flatTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], colorA);
            } else if (type == 2) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, clippedColor[0], clippedColor[1], clippedColor[2], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
                Pix3D.textureTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], clippedColor[0], clippedColor[2], clippedColor[3], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            } else if (type == 3) {
                int texturedFace = this.faceInfos[face] >> 2;
                int tA = this.texturePCoordinate[texturedFace];
                int tB = this.textureMCoordinate[texturedFace];
                int tC = this.textureNCoordinate[texturedFace];
                Pix3D.textureTriangle(x0, x1, x2, y0, y1, y2, this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
                Pix3D.textureTriangle(x0, x2, clippedX[3], y0, y2, clippedY[3], this.faceColorA[face], this.faceColorA[face], this.faceColorA[face], vertexViewSpaceX[tA], vertexViewSpaceY[tA], vertexViewSpaceZ[tA], vertexViewSpaceX[tB], vertexViewSpaceX[tC], vertexViewSpaceY[tB], vertexViewSpaceY[tC], vertexViewSpaceZ[tB], vertexViewSpaceZ[tC], this.faceColors[face]);
            }
        }
    }
}
