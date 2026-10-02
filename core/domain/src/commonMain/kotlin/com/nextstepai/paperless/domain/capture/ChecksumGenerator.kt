package com.nextstepai.paperless.domain.capture

interface ChecksumGenerator { fun sha256(bytes: ByteArray): String }
