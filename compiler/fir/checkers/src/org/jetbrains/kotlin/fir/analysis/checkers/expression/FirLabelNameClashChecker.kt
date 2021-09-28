/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.expression

import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.diagnostics.reportOn
import org.jetbrains.kotlin.fir.expressions.FirJump

object FirLabelNameClashChecker : FirJumpChecker() {
    override fun check(expression: FirJump<*>, context: CheckerContext, reporter: DiagnosticReporter) {
        if (expression.isLabeled && expression.target.shadowOuterLabels) {
            reporter.reportOn(expression.source, FirErrors.LABEL_NAME_CLASH, context)
        }
    }
}