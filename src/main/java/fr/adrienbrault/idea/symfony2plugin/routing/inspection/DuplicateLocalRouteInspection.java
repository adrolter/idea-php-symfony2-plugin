package fr.adrienbrault.idea.symfony2plugin.routing.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.util.yaml.YamlHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLDocument;
import org.jetbrains.yaml.psi.YAMLKeyValue;
import org.jetbrains.yaml.psi.YAMLMapping;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class DuplicateLocalRouteInspection extends LocalInspectionTool {

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(final @NotNull ProblemsHolder holder, boolean isOnTheFly) {
        if(!Symfony2ProjectComponent.isEnabled(holder.getProject())) {
            return super.buildVisitor(holder, isOnTheFly);
        }

        return new MyPsiElementVisitor(holder);
    }

    private static class MyPsiElementVisitor extends PsiElementVisitor {
        private final ProblemsHolder holder;

        public MyPsiElementVisitor(ProblemsHolder holder) {
            this.holder = holder;
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
            if (element instanceof YAMLKeyValue yamlKeyValue && YamlHelper.isRoutingFile(yamlKeyValue.getContainingFile()) && yamlKeyValue.getParent() instanceof YAMLMapping yamlMapping && yamlMapping.getParent() instanceof YAMLDocument) {
                String keyText1 = null;

                int found = 0;
                for (YAMLKeyValue keyValue : yamlMapping.getKeyValues()) {
                    String keyText = keyValue.getKeyText();

                    // lazy
                    if (keyText1 == null) {
                        keyText1 = yamlKeyValue.getKeyText();
                    }

                    if (keyText1.equals(keyText)) {
                        found++;
                    }

                    if (found == 2) {
                        final PsiElement keyElement = yamlKeyValue.getKey();
                        assert keyElement != null;
                        holder.registerProblem(keyElement, "Symfony: Duplicate key", ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                        break;
                    }
                }
            }

            super.visitElement(element);
        }
    }
}
