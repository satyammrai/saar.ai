package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.SaarRecap
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object SaarExportManager {

    data class ExportResult(
        val success: Boolean,
        val fileUri: Uri? = null,
        val fileName: String = "",
        val filePathDesc: String = "",
        val mimeType: String = "",
        val error: String? = null
    )

    private fun sanitizeFilename(title: String): String {
        val clean = title.replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(40)
        return if (clean.isBlank()) "Saar_Study_Notes" else "Saar_$clean"
    }

    /**
     * Generates a comprehensive, beautifully styled multi-page PDF document
     * containing ALL summary data, key takeaways, chapter notes with timestamps,
     * tables, active recall quiz, flashcards, and audio transcript.
     */
    fun exportToPdf(context: Context, recap: SaarRecap): ExportResult {
        return try {
            val fileName = "${sanitizeFilename(recap.title)}_${System.currentTimeMillis() % 10000}.pdf"
            val pdfDoc = PdfDocument()

            val pageWidth = 595
            val pageHeight = 842
            val margin = 36f
            val printableWidth = pageWidth - (margin * 2)

            var pageNumber = 1
            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var currentPage = pdfDoc.startPage(pageInfo)
            var canvas = currentPage.canvas
            var yPos = margin

            // Standard Paints
            val textPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 41, 59)
                textSize = 9.5f
            }

            val boldPaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(15, 23, 42)
                textSize = 9.5f
                isFakeBoldText = true
            }

            val titlePaint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(225, 29, 72) // Coral / Saar Red
                textSize = 17f
                isFakeBoldText = true
            }

            val h2Paint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(30, 41, 59)
                textSize = 13f
                isFakeBoldText = true
            }

            val h3Paint = TextPaint().apply {
                isAntiAlias = true
                color = Color.rgb(71, 85, 105)
                textSize = 11f
                isFakeBoldText = true
            }

            val bgPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.FILL
            }

            val strokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.rgb(226, 232, 240)
            }

            fun drawHeaderAndFooter(c: Canvas, pageNum: Int) {
                // Header accent line
                val accentPaint = Paint().apply {
                    color = Color.rgb(255, 107, 74)
                    strokeWidth = 2.5f
                }
                c.drawLine(margin, 26f, margin + 80f, 26f, accentPaint)

                val headPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = Color.rgb(148, 163, 184)
                    textSize = 8f
                }
                c.drawText("SAAR • Complete AI Study Notes & Summary", margin + 88f, 26f, headPaint)

                // Footer
                val footPaint = TextPaint().apply {
                    isAntiAlias = true
                    color = Color.rgb(148, 163, 184)
                    textSize = 8f
                }
                val pageStr = "Page $pageNum"
                val footerText = "Saar Study Companion — Watch less, understand more"
                c.drawText(footerText, margin, pageHeight - 20f, footPaint)
                val pageStrWidth = footPaint.measureText(pageStr)
                c.drawText(pageStr, pageWidth - margin - pageStrWidth, pageHeight - 20f, footPaint)

                c.drawLine(margin, pageHeight - 30f, pageWidth - margin, pageHeight - 30f, strokePaint)
            }

            fun newPage() {
                drawHeaderAndFooter(canvas, pageNumber)
                pdfDoc.finishPage(currentPage)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                currentPage = pdfDoc.startPage(pageInfo)
                canvas = currentPage.canvas
                yPos = 46f
            }

            fun ensureSpace(neededHeight: Float) {
                if (yPos + neededHeight > pageHeight - 45f) {
                    newPage()
                }
            }

            fun drawStaticText(text: String, paint: TextPaint, width: Float, align: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL): Float {
                val layout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    StaticLayout.Builder.obtain(text, 0, text.length, paint, width.toInt())
                        .setAlignment(align)
                        .setLineSpacing(1.5f, 1.15f)
                        .setIncludePad(false)
                        .build()
                } else {
                    @Suppress("DEPRECATION")
                    StaticLayout(text, paint, width.toInt(), align, 1.15f, 1.5f, false)
                }

                // If entire layout doesn't fit on this page, start new page if it's moderate size
                if (yPos + layout.height > pageHeight - 45f && layout.height < pageHeight - 90f) {
                    newPage()
                }

                canvas.save()
                canvas.translate(margin, yPos)
                layout.draw(canvas)
                canvas.restore()
                val h = layout.height.toFloat()
                yPos += h
                return h
            }

            // FIRST PAGE SETUP
            yPos = 46f

            // Document Title Banner
            bgPaint.color = Color.rgb(255, 245, 240)
            val topBannerRect = RectF(margin, yPos, margin + printableWidth, yPos + 68f)
            canvas.drawRoundRect(topBannerRect, 10f, 10f, bgPaint)

            val coralBorder = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 1.5f
                color = Color.rgb(255, 178, 150)
            }
            canvas.drawRoundRect(topBannerRect, 10f, 10f, coralBorder)

            val tagPaint = TextPaint().apply {
                color = Color.rgb(225, 29, 72)
                textSize = 8.5f
                isFakeBoldText = true
            }
            canvas.drawText("SAAR SUMMARY DOSSIER", margin + 14f, yPos + 18f, tagPaint)

            val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())
            val dateWidth = textPaint.measureText(dateStr)
            canvas.drawText(dateStr, margin + printableWidth - dateWidth - 14f, yPos + 18f, textPaint)

            // Video Title
            val titleLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(recap.title, 0, recap.title.length, titlePaint, (printableWidth - 28f).toInt())
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(recap.title, titlePaint, (printableWidth - 28f).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
            }
            canvas.save()
            canvas.translate(margin + 14f, yPos + 26f)
            titleLayout.draw(canvas)
            canvas.restore()

            yPos += 78f

            // Metadata Chips
            val metaText = "Channel: ${recap.channel}  |  Duration: ${recap.durationText}  |  Category: ${recap.category}  |  Language: ${recap.language}"
            val metaPaint = TextPaint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = 8.5f
                isFakeBoldText = true
            }
            canvas.drawText(metaText, margin, yPos, metaPaint)
            yPos += 14f

            // Video Link
            val linkPaint = TextPaint().apply {
                color = Color.rgb(2, 132, 199)
                textSize = 8f
            }
            canvas.drawText("Source Video: ${recap.videoUrl}", margin, yPos, linkPaint)
            yPos += 18f

            // 1. TL;DR (THE SAAR) SECTION
            ensureSpace(80f)
            bgPaint.color = Color.rgb(254, 249, 195) // Butter Yellow
            val tldrHeaderPaint = TextPaint().apply {
                color = Color.rgb(161, 98, 7)
                textSize = 11f
                isFakeBoldText = true
            }

            // Measure TL;DR text height
            val tldrBodyPaint = TextPaint().apply {
                color = Color.rgb(66, 32, 6)
                textSize = 9.5f
            }
            val tldrLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(recap.tldr, 0, recap.tldr.length, tldrBodyPaint, (printableWidth - 24f).toInt())
                    .setLineSpacing(1.5f, 1.15f)
                    .setIncludePad(false)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(recap.tldr, tldrBodyPaint, (printableWidth - 24f).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.15f, 1.5f, false)
            }

            val tldrBoxHeight = tldrLayout.height + 36f
            val tldrRect = RectF(margin, yPos, margin + printableWidth, yPos + tldrBoxHeight)
            canvas.drawRoundRect(tldrRect, 8f, 8f, bgPaint)

            val butterBorder = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 1f
                color = Color.rgb(250, 204, 21)
            }
            canvas.drawRoundRect(tldrRect, 8f, 8f, butterBorder)

            canvas.drawText("TL;DR (The Saar) — Core Essence 💡", margin + 12f, yPos + 18f, tldrHeaderPaint)

            canvas.save()
            canvas.translate(margin + 12f, yPos + 26f)
            tldrLayout.draw(canvas)
            canvas.restore()

            yPos += tldrBoxHeight + 16f

            // 2. KEY TAKEAWAYS
            if (recap.keyTakeaways.isNotEmpty()) {
                ensureSpace(40f)
                canvas.drawText("Key Takeaways ✨", margin, yPos, h2Paint)
                yPos += 14f

                recap.keyTakeaways.forEachIndexed { idx, takeaway ->
                    ensureSpace(30f)
                    val numBadge = "${idx + 1}."
                    canvas.drawText(numBadge, margin, yPos + 9f, boldPaint)

                    val takeawayLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(takeaway, 0, takeaway.length, textPaint, (printableWidth - 20f).toInt())
                            .setLineSpacing(1f, 1.12f)
                            .setIncludePad(false)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(takeaway, textPaint, (printableWidth - 20f).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.12f, 1f, false)
                    }

                    canvas.save()
                    canvas.translate(margin + 18f, yPos)
                    takeawayLayout.draw(canvas)
                    canvas.restore()

                    yPos += takeawayLayout.height + 6f
                }
                yPos += 12f
            }

            // 3. CHAPTER NOTES WITH TIMESTAMPS
            if (recap.notes.isNotEmpty()) {
                ensureSpace(40f)
                canvas.drawText("Detailed Chapter Breakdown 📖", margin, yPos, h2Paint)
                yPos += 16f

                recap.notes.forEach { section ->
                    ensureSpace(45f)

                    // Chapter Title Box
                    bgPaint.color = Color.rgb(241, 245, 249)
                    val sectionRect = RectF(margin, yPos - 2f, margin + printableWidth, yPos + 18f)
                    canvas.drawRoundRect(sectionRect, 4f, 4f, bgPaint)

                    canvas.drawText(section.heading, margin + 8f, yPos + 12f, h3Paint)

                    val timeBadge = "[${section.timestampFormatted}]"
                    val timePaint = TextPaint().apply {
                        color = Color.rgb(13, 148, 136)
                        textSize = 9f
                        isFakeBoldText = true
                    }
                    val timeWidth = timePaint.measureText(timeBadge)
                    canvas.drawText(timeBadge, margin + printableWidth - timeWidth - 8f, yPos + 12f, timePaint)

                    yPos += 26f

                    section.bullets.forEach { bullet ->
                        ensureSpace(24f)
                        // Bullet point circle
                        val dotPaint = Paint().apply {
                            color = Color.rgb(244, 63, 94)
                            style = Paint.Style.FILL
                        }
                        canvas.drawCircle(margin + 6f, yPos + 6f, 2.5f, dotPaint)

                        val bulletLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            StaticLayout.Builder.obtain(bullet, 0, bullet.length, textPaint, (printableWidth - 18f).toInt())
                                .setLineSpacing(1f, 1.12f)
                                .setIncludePad(false)
                                .build()
                        } else {
                            @Suppress("DEPRECATION")
                            StaticLayout(bullet, textPaint, (printableWidth - 18f).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.12f, 1f, false)
                        }

                        canvas.save()
                        canvas.translate(margin + 16f, yPos)
                        bulletLayout.draw(canvas)
                        canvas.restore()

                        yPos += bulletLayout.height + 5f
                    }
                    yPos += 10f
                }
            }

            // 4. SUMMARY & COMPARISON TABLES
            if (recap.tables.isNotEmpty()) {
                ensureSpace(50f)
                canvas.drawText("Synthesized Data & Comparison Tables 📊", margin, yPos, h2Paint)
                yPos += 16f

                recap.tables.forEach { table ->
                    ensureSpace(60f)
                    canvas.drawText(table.title, margin, yPos, h3Paint)
                    yPos += 12f

                    val colCount = table.columns.size.coerceAtLeast(1)
                    val colWidth = printableWidth / colCount
                    val cellPadding = 5f

                    // Header Row
                    bgPaint.color = Color.rgb(2, 132, 199) // Sky Blue
                    canvas.drawRect(margin, yPos, margin + printableWidth, yPos + 20f, bgPaint)

                    val tableHeadPaint = TextPaint().apply {
                        color = Color.WHITE
                        textSize = 8.5f
                        isFakeBoldText = true
                    }

                    table.columns.forEachIndexed { cIdx, col ->
                        val colLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            StaticLayout.Builder.obtain(col, 0, col.length, tableHeadPaint, (colWidth - cellPadding * 2).toInt())
                                .setIncludePad(false)
                                .build()
                        } else {
                            @Suppress("DEPRECATION")
                            StaticLayout(col, tableHeadPaint, (colWidth - cellPadding * 2).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
                        }
                        canvas.save()
                        canvas.translate(margin + (cIdx * colWidth) + cellPadding, yPos + 4f)
                        colLayout.draw(canvas)
                        canvas.restore()
                    }
                    yPos += 20f

                    // Data Rows
                    val cellPaint = TextPaint().apply {
                        color = Color.rgb(30, 41, 59)
                        textSize = 8f
                    }

                    table.rows.forEachIndexed { rIdx, row ->
                        ensureSpace(24f)
                        bgPaint.color = if (rIdx % 2 == 0) Color.rgb(248, 250, 252) else Color.WHITE
                        canvas.drawRect(margin, yPos, margin + printableWidth, yPos + 22f, bgPaint)
                        canvas.drawRect(margin, yPos, margin + printableWidth, yPos + 22f, strokePaint)

                        row.forEachIndexed { cIdx, cell ->
                            if (cIdx < colCount) {
                                val cellLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    StaticLayout.Builder.obtain(cell, 0, cell.length, cellPaint, (colWidth - cellPadding * 2).toInt())
                                        .setIncludePad(false)
                                        .build()
                                } else {
                                    @Suppress("DEPRECATION")
                                    StaticLayout(cell, cellPaint, (colWidth - cellPadding * 2).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
                                }
                                canvas.save()
                                canvas.translate(margin + (cIdx * colWidth) + cellPadding, yPos + 4f)
                                cellLayout.draw(canvas)
                                canvas.restore()
                            }
                        }
                        yPos += 22f
                    }
                    yPos += 14f
                }
            }

            // 5. ACTIVE RECALL QUIZ & EXPLANATIONS
            if (recap.quiz.isNotEmpty()) {
                ensureSpace(50f)
                canvas.drawText("Active Recall Quiz & Model Answers 🎯", margin, yPos, h2Paint)
                yPos += 16f

                recap.quiz.forEachIndexed { qIdx, item ->
                    ensureSpace(70f)
                    val qNum = "Q${qIdx + 1}: ${item.question}"
                    val qLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(qNum, 0, qNum.length, boldPaint, printableWidth.toInt())
                            .setIncludePad(false)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(qNum, boldPaint, printableWidth.toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
                    }

                    canvas.save()
                    canvas.translate(margin, yPos)
                    qLayout.draw(canvas)
                    canvas.restore()
                    yPos += qLayout.height + 6f

                    item.options.forEachIndexed { optIdx, opt ->
                        ensureSpace(18f)
                        val isCorrect = optIdx == item.answerIndex
                        val optPrefix = "${('A' + optIdx)}. $opt ${if (isCorrect) "✓ (Correct)" else ""}"
                        val optPaint = TextPaint().apply {
                            color = if (isCorrect) Color.rgb(22, 101, 52) else Color.rgb(71, 85, 105)
                            textSize = 8.5f
                            isFakeBoldText = isCorrect
                        }
                        canvas.drawText(optPrefix, margin + 12f, yPos + 9f, optPaint)
                        yPos += 15f
                    }

                    // Explanation Box
                    ensureSpace(28f)
                    bgPaint.color = Color.rgb(240, 253, 244)
                    val whyText = "💡 Explanation: ${item.why}"
                    val whyPaint = TextPaint().apply {
                        color = Color.rgb(21, 128, 61)
                        textSize = 8f
                    }
                    val whyLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        StaticLayout.Builder.obtain(whyText, 0, whyText.length, whyPaint, (printableWidth - 16f).toInt())
                            .setIncludePad(false)
                            .build()
                    } else {
                        @Suppress("DEPRECATION")
                        StaticLayout(whyText, whyPaint, (printableWidth - 16f).toInt(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)
                    }
                    val whyRect = RectF(margin + 6f, yPos, margin + printableWidth - 6f, yPos + whyLayout.height + 8f)
                    canvas.drawRoundRect(whyRect, 4f, 4f, bgPaint)

                    canvas.save()
                    canvas.translate(margin + 12f, yPos + 4f)
                    whyLayout.draw(canvas)
                    canvas.restore()

                    yPos += whyLayout.height + 14f
                }
            }

            // 6. FLASHCARDS
            if (recap.flashcards.isNotEmpty()) {
                ensureSpace(50f)
                canvas.drawText("Quick Review Flashcards 🃏", margin, yPos, h2Paint)
                yPos += 14f

                recap.flashcards.forEach { card ->
                    ensureSpace(38f)
                    bgPaint.color = Color.rgb(248, 250, 252)
                    val cardRect = RectF(margin, yPos, margin + printableWidth, yPos + 32f)
                    canvas.drawRoundRect(cardRect, 6f, 6f, bgPaint)
                    canvas.drawRoundRect(cardRect, 6f, 6f, strokePaint)

                    val frontPaint = TextPaint().apply {
                        color = Color.rgb(15, 23, 42)
                        textSize = 8.5f
                        isFakeBoldText = true
                    }
                    val backPaint = TextPaint().apply {
                        color = Color.rgb(71, 85, 105)
                        textSize = 8f
                    }

                    canvas.drawText("• [${card.topic}] ${card.front}", margin + 8f, yPos + 13f, frontPaint)
                    canvas.drawText("  → ${card.back}", margin + 8f, yPos + 25f, backPaint)

                    yPos += 36f
                }
                yPos += 10f
            }

            // 7. AUDIO ESSENCE SCRIPT
            if (recap.audioScript.isNotBlank()) {
                ensureSpace(60f)
                canvas.drawText("Audio Essence Script 🎙️", margin, yPos, h2Paint)
                yPos += 14f

                drawStaticText(recap.audioScript, textPaint, printableWidth)
                yPos += 14f
            }

            // Draw header and footer on the last page
            drawHeaderAndFooter(canvas, pageNumber)
            pdfDoc.finishPage(currentPage)

            // SAVE PDF TO FILE & DOWNLOADS
            val cacheFile = File(context.cacheDir, "exports/$fileName").apply {
                parentFile?.mkdirs()
            }
            FileOutputStream(cacheFile).use { out ->
                pdfDoc.writeTo(out)
            }
            pdfDoc.close()

            // Also copy to MediaStore Downloads so it's permanently in phone's Downloads directory
            var savedPublicUri: Uri? = null
            var publicDesc = "Downloads/Saar/$fileName"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Saar")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        cacheFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    savedPublicUri = uri
                }
            } else {
                @Suppress("DEPRECATION")
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Saar")
                if (!publicDir.exists()) publicDir.mkdirs()
                val targetFile = File(publicDir, fileName)
                cacheFile.copyTo(targetFile, overwrite = true)
                publicDesc = targetFile.absolutePath
            }

            // Provide content uri for instant viewing/sharing via FileProvider
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            ExportResult(
                success = true,
                fileUri = contentUri,
                fileName = fileName,
                filePathDesc = publicDesc,
                mimeType = "application/pdf"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(
                success = false,
                error = e.localizedMessage ?: "Unknown error creating PDF"
            )
        }
    }

    /**
     * Generates a rich Word/HTML document (.doc) containing all summary, notes,
     * tables, and quiz questions, compatible with Microsoft Word and Google Docs.
     */
    fun exportToWordDoc(context: Context, recap: SaarRecap): ExportResult {
        return try {
            val fileName = "${sanitizeFilename(recap.title)}_${System.currentTimeMillis() % 10000}.doc"

            val htmlContent = buildString {
                appendLine("<!DOCTYPE html>")
                appendLine("<html xmlns:o='urn:schemas-microsoft-com:office:office' xmlns:w='urn:schemas-microsoft-com:office:word' xmlns='http://www.w3.org/TR/REC-html40'>")
                appendLine("<head><meta charset='utf-8'><title>${recap.title}</title>")
                appendLine("<style>")
                appendLine("body { font-family: 'Segoe UI', Arial, sans-serif; line-height: 1.6; color: #1e293b; max-width: 800px; margin: 20px auto; }")
                appendLine("h1 { color: #e11d48; border-bottom: 2px solid #fecdd3; padding-bottom: 8px; font-size: 24px; }")
                appendLine("h2 { color: #0f172a; border-left: 4px solid #f43f5e; padding-left: 10px; margin-top: 28px; font-size: 18px; }")
                appendLine("h3 { color: #334155; font-size: 15px; margin-top: 18px; }")
                appendLine(".meta-box { background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px 16px; border-radius: 8px; font-size: 13px; color: #64748b; margin-bottom: 24px; }")
                appendLine(".tldr-box { background: #fef9c3; border: 1px solid #facc15; padding: 16px 20px; border-radius: 8px; margin: 20px 0; color: #713f12; }")
                appendLine(".tldr-box h3 { color: #854d0e; margin-top: 0; }")
                appendLine("table { width: 100%; border-collapse: collapse; margin: 16px 0; font-size: 13px; }")
                appendLine("th, td { border: 1px solid #cbd5e1; padding: 8px 12px; text-align: left; }")
                appendLine("th { background-color: #0284c7; color: white; font-weight: bold; }")
                appendLine("tr:nth-child(even) { background-color: #f8fafc; }")
                appendLine(".quiz-card { background: #ffffff; border: 1px solid #e2e8f0; border-radius: 8px; padding: 14px; margin: 12px 0; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }")
                appendLine(".correct-opt { color: #15803d; font-weight: bold; }")
                appendLine(".why-box { background: #f0fdf4; border: 1px solid #86efac; border-radius: 6px; padding: 8px 12px; font-size: 12px; color: #166534; margin-top: 8px; }")
                appendLine(".badge { background: #fee2e2; color: #b91c1c; padding: 2px 8px; border-radius: 4px; font-weight: bold; font-size: 11px; }")
                appendLine("ul { padding-left: 20px; }")
                appendLine("li { margin-bottom: 6px; }")
                appendLine("</style></head><body>")

                appendLine("<h1>${recap.title}</h1>")
                appendLine("<div class='meta-box'>")
                appendLine("<strong>Channel:</strong> ${recap.channel} &nbsp;|&nbsp; ")
                appendLine("<strong>Duration:</strong> ${recap.durationText} &nbsp;|&nbsp; ")
                appendLine("<strong>Category:</strong> ${recap.category} &nbsp;|&nbsp; ")
                appendLine("<strong>Language:</strong> ${recap.language}<br>")
                appendLine("<strong>Source Video:</strong> <a href='${recap.videoUrl}'>${recap.videoUrl}</a>")
                appendLine("</div>")

                appendLine("<div class='tldr-box'>")
                appendLine("<h3>TL;DR (The Saar) — Core Essence 💡</h3>")
                appendLine("<p>${recap.tldr}</p>")
                appendLine("</div>")

                if (recap.keyTakeaways.isNotEmpty()) {
                    appendLine("<h2>Key Takeaways ✨</h2><ul>")
                    recap.keyTakeaways.forEach { takeaway ->
                        appendLine("<li>$takeaway</li>")
                    }
                    appendLine("</ul>")
                }

                if (recap.notes.isNotEmpty()) {
                    appendLine("<h2>Detailed Chapter Breakdown 📖</h2>")
                    recap.notes.forEach { s ->
                        appendLine("<h3>${s.heading} <span class='badge'>[${s.timestampFormatted}]</span></h3><ul>")
                        s.bullets.forEach { b ->
                            appendLine("<li>$b</li>")
                        }
                        appendLine("</ul>")
                    }
                }

                if (recap.tables.isNotEmpty()) {
                    appendLine("<h2>Synthesized Tables 📊</h2>")
                    recap.tables.forEach { table ->
                        appendLine("<h3>${table.title}</h3>")
                        appendLine("<table><thead><tr>")
                        table.columns.forEach { col -> appendLine("<th>$col</th>") }
                        appendLine("</tr></thead><tbody>")
                        table.rows.forEach { row ->
                            appendLine("<tr>")
                            row.forEach { cell -> appendLine("<td>$cell</td>") }
                            appendLine("</tr>")
                        }
                        appendLine("</tbody></table>")
                    }
                }

                if (recap.quiz.isNotEmpty()) {
                    appendLine("<h2>Active Recall Quiz 🎯</h2>")
                    recap.quiz.forEachIndexed { idx, q ->
                        appendLine("<div class='quiz-card'>")
                        appendLine("<strong>Q${idx + 1}: ${q.question}</strong><ul>")
                        q.options.forEachIndexed { optIdx, opt ->
                            val isCorrect = optIdx == q.answerIndex
                            if (isCorrect) {
                                appendLine("<li class='correct-opt'>${('A' + optIdx)}. $opt &nbsp;✓ <em>(Correct)</em></li>")
                            } else {
                                appendLine("<li>${('A' + optIdx)}. $opt</li>")
                            }
                        }
                        appendLine("</ul>")
                        appendLine("<div class='why-box'>💡 <strong>Explanation:</strong> ${q.why}</div>")
                        appendLine("</div>")
                    }
                }

                if (recap.flashcards.isNotEmpty()) {
                    appendLine("<h2>Quick Flashcards 🃏</h2><ul>")
                    recap.flashcards.forEach { card ->
                        appendLine("<li><strong>[${card.topic}] ${card.front}</strong><br><em>Answer:</em> ${card.back}</li>")
                    }
                    appendLine("</ul>")
                }

                if (recap.audioScript.isNotBlank()) {
                    appendLine("<h2>Audio Essence Script 🎙️</h2>")
                    appendLine("<p style='font-style: italic; color: #475569;'>${recap.audioScript}</p>")
                }

                appendLine("<hr style='margin-top: 40px; border: none; border-top: 1px solid #cbd5e1;'>")
                appendLine("<p style='font-size: 11px; color: #94a3b8; text-align: center;'>Generated by Saar App — Watch less, understand more.</p>")
                appendLine("</body></html>")
            }

            val cacheFile = File(context.cacheDir, "exports/$fileName").apply {
                parentFile?.mkdirs()
                writeText(htmlContent, Charsets.UTF_8)
            }

            var savedPublicUri: Uri? = null
            var publicDesc = "Downloads/Saar/$fileName"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/msword")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Saar")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        cacheFile.inputStream().use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                    savedPublicUri = uri
                }
            } else {
                @Suppress("DEPRECATION")
                val publicDir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Saar")
                if (!publicDir.exists()) publicDir.mkdirs()
                val targetFile = File(publicDir, fileName)
                cacheFile.copyTo(targetFile, overwrite = true)
                publicDesc = targetFile.absolutePath
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                cacheFile
            )

            ExportResult(
                success = true,
                fileUri = contentUri,
                fileName = fileName,
                filePathDesc = publicDesc,
                mimeType = "application/msword"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ExportResult(
                success = false,
                error = e.localizedMessage ?: "Unknown error creating document"
            )
        }
    }

    /**
     * Opens the exported document with any installed PDF viewer or Office viewer.
     */
    fun openFile(context: Context, uri: Uri, mimeType: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Open Notes with"))
        } catch (e: Exception) {
            Toast.makeText(context, "No app found to open this document. File saved in Downloads folder!", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Shares the exported document via WhatsApp, Google Drive, Email, etc.
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String, title: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Saar Notes: $title")
                putExtra(Intent.EXTRA_TEXT, "Here are the complete Saar study notes for '$title'.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Share Saar Notes"))
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open share sheet", Toast.LENGTH_SHORT).show()
        }
    }
}
