package siosio.doma.inspection.dao

import com.intellij.codeInspection.*
import com.intellij.codeInspection.compiler.*
import com.intellij.psi.util.*
import siosio.doma.*

val selectMethodRule =
    rule {
      sql(true)

      // SelectOptionsの検査
      parameterRule { problemsHolder, method ->
        val selectOptions = filter {
          "org.seasar.doma.jdbc.SelectOptions".equals(it.getType().getCanonicalText())
        }
        if (selectOptions.size !in 0..1) {
          selectOptions.forEach {
            problemsHolder.registerProblem(
                it,
                DomaBundle.message("inspection.dao.multi-SelectOptions"),
                ProblemHighlightType.ERROR,
                RemoveElementQuickFix(DomaBundle.message("quick-fix.remove", it.getName())))
          }
        }
      }

      // strategyにSTREAMを指定した場合の検査
      parameterRule { problemsHolder, method ->
        val daoAnnotation = method.daoAnnotation
        daoAnnotation.findAttributeValue("strategy")?.let { strategy ->
          if (!strategy.text.contains("STREAM")) {
            return@parameterRule;
          }
          val function = filter {
            PsiTypesUtil.getPsiClass(it.type)?.qualifiedName == "java.util.function.Function"
          }
          if (function.size != 1) {
            problemsHolder.registerProblem(
                daoAnnotation.originalElement,
                DomaBundle.message("inspection.dao.function-strategy"),
                ProblemHighlightType.ERROR)
          }
        }
      }
    }