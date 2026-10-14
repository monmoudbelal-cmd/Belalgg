package com.example.data.utils

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.data.dao.EduDao
import com.example.data.model.CategoryEntity
import com.example.data.model.FileItemEntity
import com.example.data.model.QuizEntity
import com.example.data.model.QuizQuestionEntity
import com.example.data.model.UserAccountEntity
import java.io.File
import java.io.FileOutputStream

object SampleDataGenerator {

    /**
     * Generates a genuine multi-page PDF on local disk so PdfRenderer works out-of-the-box.
     */
    fun createSamplePdf(context: Context, filename: String, subjectTitle: String): String {
        val file = File(context.filesDir, filename)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        try {
            val document = PdfDocument()

            // Page 1
            val pageInfo1 = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page1 = document.startPage(pageInfo1)
            val canvas1 = page1.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(30, 58, 138)
                textSize = 24f
                isFakeBoldText = true
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(71, 85, 105)
                textSize = 14f
                isAntiAlias = true
                textAlign = Paint.Align.CENTER
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(15, 23, 42)
                textSize = 13f
                isAntiAlias = true
                textAlign = Paint.Align.RIGHT
            }

            val boxPaint = Paint().apply {
                color = Color.rgb(238, 242, 255)
                style = Paint.Style.FILL
            }
            val borderPaint = Paint().apply {
                color = Color.rgb(99, 102, 241)
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }

            // Draw header banner
            canvas1.drawRect(40f, 40f, 555f, 120f, boxPaint)
            canvas1.drawRect(40f, 40f, 555f, 120f, borderPaint)
            canvas1.drawText("ملخص تعليمي: $subjectTitle", 595f / 2, 80f, titlePaint)
            canvas1.drawText("منصة EduVault التعليمية - مادة ومراجعة شاملة", 595f / 2, 105f, subtitlePaint)

            var yOffset = 160f
            val points = listOf(
                "• الوحدة الأولى: المفاهيم الأساسية والقوانين الرياضية والفيزيائية المهمة.",
                "• خطوات حل المسائل: تحديد المعطيات، القانون المستخدم، والتعويض المباشر.",
                "• تطبيقات عملية: أمثلة توضيحية لتعزيز فهم الطالب قبل دخول الكويز.",
                "• مراجعة سريعة لأهم النظريات والقواعد التي تتكرر في الاختبارات الفصلية.",
                "• نصيحة المدرس: راجع هذا الملخص جيداً ثم توجه لحل الكويز الخاص بالقسم."
            )

            for (point in points) {
                canvas1.drawText(point, 545f, yOffset, bodyPaint)
                yOffset += 40f
            }

            // Footer
            canvas1.drawText("الصفحة 1 من 2 - تم إعدادها بواسطة منصة EduVault", 595f / 2, 800f, subtitlePaint)
            document.finishPage(page1)

            // Page 2
            val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
            val page2 = document.startPage(pageInfo2)
            val canvas2 = page2.canvas

            canvas2.drawText("ملحق التمارين والقوانين الإضافية", 595f / 2, 70f, titlePaint)
            var y2 = 120f
            val page2Points = listOf(
                "1. القانون العام وتطبيقاته على الحركة والميكانيكا والدوائر الكهربائية.",
                "2. جدول التحويلات والوحدات المعيارية الدولية.",
                "3. ملاحظات هامة: تأكد دائماً من كتابة خطوات الحل كاملة.",
                "4. بالتوفيق والنجاح لجميع الطلاب والطالبات!"
            )
            for (p in page2Points) {
                canvas2.drawText(p, 545f, y2, bodyPaint)
                y2 += 40f
            }
            canvas2.drawText("الصفحة 2 من 2 - انتهى الملخص", 595f / 2, 800f, subtitlePaint)
            document.finishPage(page2)

            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return file.absolutePath
    }

    suspend fun populateInitialDataIfEmpty(context: Context, dao: EduDao) {
        if (dao.countUsers() > 0) return

        // 1. Create Default Categories
        val mathCatId = dao.insertCategory(
            CategoryEntity(
                name = "الرياضيات المتقدمة",
                description = "التفاضل، التكامل، الجبر والهندسة التحليلية",
                colorHex = "#2563EB",
                iconName = "calculate",
                orderIndex = 0
            )
        )

        val physicsCatId = dao.insertCategory(
            CategoryEntity(
                name = "الفيزياء الحديثة",
                description = "الميكانيكا، الكهرومغناطيسية، والفيزياء الذرية",
                colorHex = "#059669",
                iconName = "science",
                orderIndex = 1
            )
        )

        val englishCatId = dao.insertCategory(
            CategoryEntity(
                name = "اللغة الإنجليزية",
                description = "قواعد اللغة، المفردات، والمحادثة المتقدمة",
                colorHex = "#7C3AED",
                iconName = "menu_book",
                orderIndex = 2
            )
        )

        // 2. Generate Real PDFs
        val mathPdfPath = createSamplePdf(context, "math_notes.pdf", "الرياضيات المتقدمة")
        val physicsPdfPath = createSamplePdf(context, "physics_summary.pdf", "الفيزياء الحديثة")
        val englishPdfPath = createSamplePdf(context, "english_grammar.pdf", "قواعد الإنجليزية")

        // 3. Add Sample Files
        // Math Files
        dao.insertFile(
            FileItemEntity(
                categoryId = mathCatId,
                title = "مذكرة التفاضل والتكامل الشاملة 2026",
                description = "ملخص قوانين النهايات والتكامل غير المحدد مع مسائل محلولة",
                fileType = "PDF",
                uriString = mathPdfPath,
                fileSize = "1.8 MB",
                extraContent = "مذكرة تفاعلية تتضمن مراجعة القوانين الأساسية والتطبيقات الهندسية."
            )
        )
        dao.insertFile(
            FileItemEntity(
                categoryId = mathCatId,
                title = "شرح فيديو: تطبيقات القيم العظمى والصغرى",
                description = "شرح مسجل بالفيديو لحل المسائل الفيزيائية والهندسية",
                fileType = "VIDEO",
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                fileSize = "14.2 MB",
                extraContent = "محاضرة مسجلة مدتها 5 دقائق توضح كيفية إيجاد النقاط الحرجة وتصنيفها."
            )
        )
        dao.insertFile(
            FileItemEntity(
                categoryId = mathCatId,
                title = "خريطة المفاهيم: الدوال والمصفوفات",
                description = "مخطط بصري ذكي يوضح ترابط مفاهيم الجبر الخطي",
                fileType = "IMAGE",
                uriString = "res://drawable/img_edu_hero",
                fileSize = "620 KB",
                extraContent = "صورة توضيحية لخرائط المفاهيم لتسهيل الاستذكار السريع."
            )
        )

        // Physics Files
        dao.insertFile(
            FileItemEntity(
                categoryId = physicsCatId,
                title = "ملخص قوانين الكهربية والمغناطيسية",
                description = "ورقة القوانين الذهبية لمسائل كيرشوف وقانون أوم",
                fileType = "PDF",
                uriString = physicsPdfPath,
                fileSize = "2.1 MB",
                extraContent = "ملف PDF مصمم لطلاب الثانوية والجامعة للمراجعة قبل الاختبار النهائي."
            )
        )
        dao.insertFile(
            FileItemEntity(
                categoryId = physicsCatId,
                title = "فيديو تجربة عملية: تأثير المجال المغناطيسي",
                description = "تصوير عملي لحركة الشحنات وتطبيق قاعدة اليد اليمنى",
                fileType = "VIDEO",
                uriString = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                fileSize = "25.0 MB",
                extraContent = "تجربة مختبرية عالية الدقة لتوضيح توليد القوة الدافعة الكهربية."
            )
        )

        // English Files
        dao.insertFile(
            FileItemEntity(
                categoryId = englishCatId,
                title = "Complete English Tenses Guide",
                description = "دليل الأزمنة والأفعال الشاذة مع أمثلة عملية",
                fileType = "PDF",
                uriString = englishPdfPath,
                fileSize = "1.2 MB",
                extraContent = "شرح مبسّط للأزمنة واستخداماتها اليومية."
            )
        )

        // 4. Create User Accounts (The user specified: "لو حد مع نفس البرنامج يقدر يخش علي الملفات ولكن بكلمة سر أنا واسم مستخدم وهكذا اقدر اعمل كتير من ده")
        dao.insertUser(
            UserAccountEntity(
                username = "ahmed",
                password = "123",
                displayName = "أحمد محمد علي",
                allowedCategoryIds = "*",
                isActive = true,
                notes = "طالب متميز - تصريح لكافة المواد"
            )
        )

        dao.insertUser(
            UserAccountEntity(
                username = "sara",
                password = "456",
                displayName = "سارة إبراهيم",
                allowedCategoryIds = "$mathCatId,$physicsCatId",
                isActive = true,
                notes = "مسجلة في الرياضيات والفيزياء فقط"
            )
        )

        dao.insertUser(
            UserAccountEntity(
                username = "omar",
                password = "789",
                displayName = "عمر خالد",
                allowedCategoryIds = "$englishCatId",
                isActive = true,
                notes = "مسجل في كورس اللغة الإنجليزية"
            )
        )

        // 5. Create Interactive Quizzes
        val mathQuizId = dao.insertQuiz(
            QuizEntity(
                categoryId = mathCatId,
                title = "اختبار قصير: التفاضل والتكامل",
                description = "اختبار تفاعلي لقياس استيعاب القوانين الأساسية",
                durationMinutes = 10,
                passScorePercent = 60
            )
        )

        dao.insertQuestions(
            listOf(
                QuizQuestionEntity(
                    quizId = mathQuizId,
                    questionText = "ما هي مشتقة الدالة f(x) = 3x² + 5x - 7 ؟",
                    optionA = "6x + 5",
                    optionB = "3x + 5",
                    optionC = "6x - 7",
                    optionD = "x² + 5",
                    correctOptionIndex = 0,
                    explanation = "تطبيق قاعدة مشتقة القوة: d/dx(ax^n) = a*n*x^(n-1). إذن 3*2x + 5 = 6x + 5.",
                    orderIndex = 0
                ),
                QuizQuestionEntity(
                    quizId = mathQuizId,
                    questionText = "ما هي قيمة تكامل ∫ 4x dx ؟",
                    optionA = "4x² + C",
                    optionB = "2x² + C",
                    optionC = "4 + C",
                    optionD = "x² + C",
                    correctOptionIndex = 1,
                    explanation = "تكامل x هو x²/2، مضروباً في 4 ينتج 4 * (x²/2) = 2x² + C.",
                    orderIndex = 1
                ),
                QuizQuestionEntity(
                    quizId = mathQuizId,
                    questionText = "ميل المماس لمنحنى الدالة عند أي نقطة يمثل:",
                    optionA = "التكامل المحدد للدالة",
                    optionB = "متوسط التغير في فترة",
                    optionC = "المشتقة الأولى للدالة",
                    optionD = "المشتقة الثانية للدالة",
                    correctOptionIndex = 2,
                    explanation = "المشتقة الأولى f'(x) هي التعبير الهندسي عن ميل المماس لمنحنى الدالة عند أي نقطة.",
                    orderIndex = 2
                )
            )
        )

        val physicsQuizId = dao.insertQuiz(
            QuizEntity(
                categoryId = physicsCatId,
                title = "كويز الكهربية وقانون أوم",
                description = "تحدي قياس المقاومات والتيار وفرق الجهد",
                durationMinutes = 8,
                passScorePercent = 70
            )
        )

        dao.insertQuestions(
            listOf(
                QuizQuestionEntity(
                    quizId = physicsQuizId,
                    questionText = "إذا كان فرق الجهد 12V والمقاومة 4Ω، فما شدة التيار الكهربي؟",
                    optionA = "48 A",
                    optionB = "8 A",
                    optionC = "3 A",
                    optionD = "0.33 A",
                    correctOptionIndex = 2,
                    explanation = "طبق قانون أوم: I = V / R = 12 / 4 = 3 أمبير.",
                    orderIndex = 0
                ),
                QuizQuestionEntity(
                    quizId = physicsQuizId,
                    questionText = "عند توصيل مقاومتين متساويتين على التوازي، فإن المقاومة المكافئة تكون:",
                    optionA = "ضعف قيمة إحداهما",
                    optionB = "نصف قيمة إحداهما",
                    optionC = "مساوية لقيمة إحداهما",
                    optionD = "صفر",
                    correctOptionIndex = 1,
                    explanation = "في التوصيل على التوازي لمقاومات متساوية: R_eq = R / n = R / 2.",
                    orderIndex = 1
                )
            )
        )
    }
}
