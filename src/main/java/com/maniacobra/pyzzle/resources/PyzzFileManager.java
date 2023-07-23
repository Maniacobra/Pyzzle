package com.maniacobra.pyzzle.resources;

import com.maniacobra.pyzzle.properties.AppProperties;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.spec.KeySpec;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PyzzFileManager {

    private static final PyzzFileManager instance = new PyzzFileManager();

    public static PyzzFileManager getInstance() {
        return instance;
    }

    // CRYPTO

    private static final String password = "OarhCkfmJZlGmwMvdgoZViMhzrMVmqcQ";
    private static final int keySize = 128;
    private static final int pswdIterations = 65536;
    private static final String symmetricAlgorithm = "AES/CBC/PKCS5Padding";
    private static final String secretKeyFactoryAlgorithm = "PBKDF2WithHmacSHA1";

    private static final byte[] salt = "1132695652610781".getBytes();
    private static final byte[] iv = "3180167959068887".getBytes();

    // CLASS

    private PyzzFileManager() {
    }

    private SecretKeySpec createSecretKey() throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, pswdIterations, keySize);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(secretKeyFactoryAlgorithm);
        byte[] key = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(key, "AES");
    }

    public String readNormal(File file) throws IOException {

        String content = Files.readString(file.toPath(), StandardCharsets.UTF_8);
        String[] path = file.getAbsolutePath().split("\\.");
        // Auto encode
        if (path.length > 1 && path[path.length - 1].equals("json")) {
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < path.length; i++) {
                if (i == path.length - 1) {
                    strBuilder.append(".");
                    strBuilder.append(AppProperties.extension);
                }
                else
                    strBuilder.append(path[i]);
            }
            File newFile = new File(strBuilder.toString());
            try {
                encode(newFile, content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return content;
    }

    public void encode(File file, String content) throws Exception {
        SecretKeySpec secretKeySpec = createSecretKey();
        Cipher cipher = Cipher.getInstance(symmetricAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));
        byte[] encryptedContent = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));

        try (FileOutputStream output = new FileOutputStream(file);
             GZIPOutputStream gzipOut = new GZIPOutputStream(output);
             OutputStreamWriter writer = new OutputStreamWriter(gzipOut, StandardCharsets.UTF_8)) {
            writer.write(Base64.getEncoder().encodeToString(encryptedContent));
        }
    }

    public String decode(File file) throws Exception {
        SecretKeySpec secretKeySpec = createSecretKey();
        Cipher cipher = Cipher.getInstance(symmetricAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(iv));

        try (FileInputStream input = new FileInputStream(file);
             GZIPInputStream gzipIn = new GZIPInputStream(input);
             InputStreamReader reader = new InputStreamReader(gzipIn, StandardCharsets.UTF_8);
             BufferedReader bufferedReader = new BufferedReader(reader)) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            byte[] decryptedContent = cipher.doFinal(Base64.getDecoder().decode(builder.toString()));
            return new String(decryptedContent, StandardCharsets.UTF_8);
        }
    }
}
