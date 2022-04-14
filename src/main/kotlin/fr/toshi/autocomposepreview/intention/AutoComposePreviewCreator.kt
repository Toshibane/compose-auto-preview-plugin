package fr.toshi.autocomposepreview.intention

import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtPsiFactory

class AutoComposePreviewCreator: PsiElementBaseIntentionAction(), IntentionAction {
    override fun getText(): String = "Generate Compose Preview"
    override fun getFamilyName(): String = text

    override fun isAvailable(project: Project, editor: Editor, element: PsiElement): Boolean {
        val sourceFunction =
                element as? KtNamedFunction ?: PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
                ?: return false

        sourceFunction.annotationEntries.forEach {
            if (it.shortName?.asString()?.contains("Composable") == true || it.text.contains("Composable") || it.name?.contains("Composable") == true) {
                return true
            }
        }

        return false
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        val sourceFunction =
            element as? KtNamedFunction ?: PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java)
            ?: return
        val nameNewFunction = "Preview" + sourceFunction.name?.capitalize()

        WriteCommandAction.writeCommandAction(project, element.containingFile).run<Throwable> {
            val factory = KtPsiFactory(project)

            val functionBody = "@Preview\n@Composable\nfun $nameNewFunction() {\n${sourceFunction.name}()\n}"

            val newFunction = element.containingFile.add(factory.createFunction(functionBody))

            editor?.caretModel?.primaryCaret?.moveToOffset(newFunction.startOffset + functionBody.length - 3)
        }
    }

}