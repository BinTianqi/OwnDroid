package com.bintianqi.owndroid.feature.system

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class CaCertViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val toastChannel: ToastChannel
) : ViewModel() {
    val installedCertsState = MutableStateFlow(emptyList<CaCertInfo>())

    val selectedCert = MutableStateFlow<CaCertInfo?>(null)

    fun getCaCerts() = ph.safeDpmCall {
        installedCertsState.value = dpm.getInstalledCaCerts(dar).mapNotNull { parseCert(it) }
    }

    fun selectCert(cert: CaCertInfo) {
        selectedCert.value = cert
    }

    fun parseCert(uri: Uri) {
        try {
            application.contentResolver.openInputStream(uri)?.use {
                selectedCert.value = parseCert(it.readBytes())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastChannel.sendStatus(false)
        }
    }

    private fun parseCert(bytes: ByteArray): CaCertInfo {
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).toHexString()
        val factory = CertificateFactory.getInstance("X.509")
        val cert = factory.generateCertificate(bytes.inputStream()) as X509Certificate
        return CaCertInfo(
            hash, cert.serialNumber.toString(16),
            cert.issuerX500Principal.name, cert.subjectX500Principal.name,
            cert.notBefore.time, cert.notAfter.time, bytes
        )
    }

    fun installCert() = ph.safeDpmCall {
        val result = dpm.installCaCert(dar, selectedCert.value!!.bytes)
        if (result) getCaCerts()
        toastChannel.sendStatus(result)
    }

    fun uninstallCert() = ph.safeDpmCall {
        dpm.uninstallCaCert(dar, selectedCert.value!!.bytes)
        getCaCerts()
    }

    fun uninstallAll() = ph.safeDpmCall {
        dpm.uninstallAllUserCaCerts(dar)
        installedCertsState.value = emptyList()
    }

    fun exportCert(uri: Uri) {
        application.contentResolver.openOutputStream(uri)?.use {
            it.write(selectedCert.value!!.bytes)
        }
        toastChannel.sendStatus(true)
    }
}
