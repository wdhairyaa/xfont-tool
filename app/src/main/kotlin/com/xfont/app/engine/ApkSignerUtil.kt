package com.xfont.app.engine

import android.content.Context
import com.android.apksig.ApkSigner
import java.io.File
import java.security.KeyStore
import java.security.PrivateKey
import java.security.cert.X509Certificate

object ApkSignerUtil {

    private const val KEYSTORE_ASSET_PATH = "keys/monotype.jks"
    private const val KEY_ALIAS = "monotype"
    private const val KEY_PASS = "xfont123"

    @Synchronized
    fun getSigningKey(context: Context): Pair<PrivateKey, List<X509Certificate>> {
        val keyStore = KeyStore.getInstance("PKCS12")
        context.assets.open(KEYSTORE_ASSET_PATH).use { input ->
            keyStore.load(input, KEY_PASS.toCharArray())
        }
        val privateKey = keyStore.getKey(KEY_ALIAS, KEY_PASS.toCharArray()) as PrivateKey
        val certChain = keyStore.getCertificateChain(KEY_ALIAS).map { it as X509Certificate }
        return Pair(privateKey, certChain)
    }

    @Suppress("DEPRECATION")
    fun signApk(context: Context, inputApk: File, outputApk: File) {
        val (privateKey, certChain) = getSigningKey(context)

        val signerConfig = ApkSigner.SignerConfig.Builder(
            "monotype",
            privateKey,
            certChain
        ).build()

        val apkSigner = ApkSigner.Builder(listOf(signerConfig))
            .setInputApk(inputApk)
            .setOutputApk(outputApk)
            .setV1SigningEnabled(true)
            .setV2SigningEnabled(true)
            .setV3SigningEnabled(true)
            .build()

        apkSigner.sign()
    }
}
