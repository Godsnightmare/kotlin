/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import org.jetbrains.kotlin.analysis.api.components.KtOverrideInfoProvider
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.getSymbolDescriptor
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtCallableSymbol
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.toKtClassifierSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtClassOrObjectSymbol
import org.jetbrains.kotlin.analysis.api.tokens.ValidityToken
import org.jetbrains.kotlin.analysis.api.withValidityAssertion
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.DeclarationDescriptorWithVisibility
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.resolve.findOriginalTopMostOverriddenDescriptors
import org.jetbrains.kotlin.util.ImplementationStatus

internal class KtFe10OverrideInfoProvider(
    override val analysisSession: KtFe10AnalysisSession
) : KtOverrideInfoProvider(), Fe10KtAnalysisSessionComponent {
    override val token: ValidityToken
        get() = analysisSession.token

    override fun isVisible(memberSymbol: KtCallableSymbol, classSymbol: KtClassOrObjectSymbol): Boolean = withValidityAssertion {
        val memberDescriptor = getSymbolDescriptor(memberSymbol) as? DeclarationDescriptorWithVisibility ?: return false
        val classDescriptor = getSymbolDescriptor(classSymbol) ?: return false
        return DescriptorVisibilities.isVisibleWithAnyReceiver(memberDescriptor, classDescriptor)
    }

    override fun getImplementationStatus(
        memberSymbol: KtCallableSymbol,
        parentClassSymbol: KtClassOrObjectSymbol
    ): ImplementationStatus? = withValidityAssertion {
        throw NotImplementedError("Method is not implemented for FE 1.0")
    }

    override fun getOriginalOverriddenSymbol(symbol: KtCallableSymbol): KtCallableSymbol? = withValidityAssertion {
        val callableDescriptor = getSymbolDescriptor(symbol) as? CallableMemberDescriptor ?: return null
        val originalCallableDescriptor = callableDescriptor.findOriginalTopMostOverriddenDescriptors().firstOrNull() ?: return null
        return originalCallableDescriptor.toKtCallableSymbol(analysisContext)
    }

    override fun getOriginalContainingClassForOverride(symbol: KtCallableSymbol): KtClassOrObjectSymbol? = withValidityAssertion {
        val callableDescriptor = getSymbolDescriptor(symbol) as? CallableMemberDescriptor ?: return null
        val originalCallableDescriptor = callableDescriptor.findOriginalTopMostOverriddenDescriptors().firstOrNull() ?: return null
        val containingClassDescriptor = originalCallableDescriptor.containingDeclaration as? ClassDescriptor ?: return null
        return containingClassDescriptor.toKtClassifierSymbol(analysisContext) as? KtClassOrObjectSymbol
    }
}