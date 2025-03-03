/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.impl.base.test.components

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.components.KtTypeRendererOptions
import org.jetbrains.kotlin.analysis.api.impl.barebone.parentsOfType
import org.jetbrains.kotlin.analysis.api.impl.barebone.test.FrontendApiTestConfiguratorService
import org.jetbrains.kotlin.analysis.api.impl.barebone.test.expressionMarkerProvider
import org.jetbrains.kotlin.analysis.api.impl.base.test.test.framework.AbstractHLApiSingleModuleTest
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.test.model.TestModule
import org.jetbrains.kotlin.test.services.TestServices
import org.jetbrains.kotlin.test.services.assertions

abstract class AbstractOverriddenDeclarationProviderTest(
    configurator: FrontendApiTestConfiguratorService
) : AbstractHLApiSingleModuleTest(configurator) {
    override fun doTestByFileStructure(ktFiles: List<KtFile>, module: TestModule, testServices: TestServices) {
        super.doTestByFileStructure(ktFiles, module, testServices)

        val declaration = testServices.expressionMarkerProvider.getElementOfTypAtCaret<KtDeclaration>(ktFiles.first())

        val actual = executeOnPooledThreadInReadAction {
            analyseForTest(declaration) {
                val symbol = declaration.getSymbol() as KtCallableSymbol
                val allOverriddenSymbols = symbol.getAllOverriddenSymbols().map { renderSignature(it) }
                val directlyOverriddenSymbols = symbol.getDirectlyOverriddenSymbols().map { renderSignature(it) }
                buildString {
                    appendLine("ALL:")
                    allOverriddenSymbols.forEach { appendLine("  $it") }
                    appendLine("DIRECT:")
                    directlyOverriddenSymbols.forEach { appendLine("  $it") }
                }
            }
        }
        testServices.assertions.assertEqualsToTestDataFileSibling(actual)
    }

    private fun KtAnalysisSession.renderSignature(symbol: KtCallableSymbol): String = buildString {
        append(getPath(symbol))
        if (symbol is KtFunctionSymbol) {
            append("(")
            symbol.valueParameters.forEachIndexed { index, parameter ->
                append(parameter.name.identifier)
                append(": ")
                append(parameter.annotatedType.type.render(KtTypeRendererOptions.SHORT_NAMES))
                if (index != symbol.valueParameters.lastIndex) {
                    append(", ")
                }
            }
            append(")")
        }
        append(": ")
        append(symbol.annotatedType.type.render(KtTypeRendererOptions.SHORT_NAMES))
    }

    private fun getPath(symbol: KtCallableSymbol): String = when (symbol) {
        is KtSyntheticJavaPropertySymbol -> symbol.callableIdIfNonLocal?.toString()!!
        else -> {
            val ktDeclaration = symbol.psi as KtDeclaration
            ktDeclaration
                .parentsOfType<KtDeclaration>(withSelf = true)
                .map { it.name ?: "<no name>" }
                .toList()
                .asReversed()
                .joinToString(separator = ".")
        }
    }
}