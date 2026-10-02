package com.nextstepai.paperless.domain.capture

import java.security.MessageDigest

class PlatformChecksumGenerator : ChecksumGenerator {
    override fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
}
