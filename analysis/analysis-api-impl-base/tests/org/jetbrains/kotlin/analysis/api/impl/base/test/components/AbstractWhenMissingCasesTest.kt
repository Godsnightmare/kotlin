/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.components

import org.jetbrains.kotlin.analysis.api.impl.barebone.test.FrontendApiTestConfiguratorService
import org.jetbrains.kotlin.analysis.api.impl.barebone.test.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.api.impl.base.test.test.framework.AbstractHLApiSingleFileTest
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractWhenMissingCasesTest(
    configurator: FrontendApiTestConfiguratorService
) : AbstractHLApiSingleFileTest(configurator) {
    override fun doTestByFileStructure(ktFile: KtFile, module: TestModule, testServices: TestServices) {
        super.doTestByFileStructure(ktFile, module, testServices)

        val whenExpression = testServices.expressionMarkerProvider.getElementOfTypAtCaret<KtWhenExpression>(ktFile)

        val actual = executeOnPooledThreadInReadAction {
            analyseForTest(whenExpression) {
                buildString {
                    for (missingCase in whenExpression.getMissingCases()) {
                        appendLine(missingCase::class.simpleName + " - " + missingCase.branchConditionText)
                    }
                }
            }
        }

        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }
}