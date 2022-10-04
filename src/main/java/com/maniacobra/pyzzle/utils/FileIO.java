package com.maniacobra.pyzzle.utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

public class FileIO {

    private static final int[] key = {
            96, -27, 95, -3,
            -1, 30, -6, -28,
            1, -25, 67, 47,
            25, 15, 88, 90
    };

    private static final FileIO instance = new FileIO();

    public static FileIO getInstance() {
        return instance;
    }

    // CLASS

    public void encrypt(File file, String content) {

        try (FileWriter writer = new FileWriter(file)) {
            int i = 0;
            for (byte val : content.getBytes(StandardCharsets.UTF_8)) {
                val += key[i];
                writer.write(val);
                i++;
                i %= key.length;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(File file) throws IOException {

        try (FileReader reader = new FileReader(file)) {
            ArrayList<Byte> bytes = new ArrayList<>();
            byte val;
            int i = 0;
            while ((val = (byte) reader.read()) != -1) {
                val -= key[i];
                bytes.add(val);
                i++;
                i %= key.length;
            }
            byte[] arr = new byte[bytes.size()];
            i = 0;
            for (Byte b : bytes) {
                arr[i] = b;
                i++;
            }
            return new String(arr, StandardCharsets.UTF_8);
        }
    }

    public String readNormal(File file) {
        String content = null;
        try {
            content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }
}
