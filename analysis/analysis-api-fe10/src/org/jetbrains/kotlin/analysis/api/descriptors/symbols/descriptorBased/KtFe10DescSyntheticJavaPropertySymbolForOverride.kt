/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased

import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisContext
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.descriptorBased.base.*
import org.jetbrains.kotlin.analysis.api.descriptors.symbols.pointers.KtFe10NeverRestoringSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.KtFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertyGetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertySetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSyntheticJavaPropertySymbol
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtConstantValue
import org.jetbrains.kotlin.analysis.api.symbols.markers.KtTypeAndAnnotations
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.withValidityAssertion
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.load.java.descriptors.JavaForKotlinOverridePropertyDescriptor
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.isExtension

internal class KtFe10DescSyntheticJavaPropertySymbolForOverride(
    override val descriptor: JavaForKotlinOverridePropertyDescriptor,
    override val analysisContext: Fe10AnalysisContext
) : KtSyntheticJavaPropertySymbol(), KtFe10DescMemberSymbol<JavaForKotlinOverridePropertyDescriptor> {
    override val name: Name
        get() = withValidityAssertion { descriptor.name }

    override val isFromPrimaryConstructor: Boolean
        get() = withValidityAssertion { descriptor.containingDeclaration is ConstructorDescriptor }

    override val isOverride: Boolean
        get() = withValidityAssertion { descriptor.isExplicitOverride }

    override val isStatic: Boolean
        get() = withValidityAssertion { DescriptorUtils.isStaticDeclaration(descriptor) }

    override val isVal: Boolean
        get() = withValidityAssertion { !descriptor.isVar }

    override val isExtension: Boolean
        get() = withValidityAssertion { descriptor.isExtension }

    override val getter: KtPropertyGetterSymbol
        get() = withValidityAssertion {
            val getter = descriptor.getter ?: return KtFe10DescDefaultPropertyGetterSymbol(descriptor, analysisContext)
            return KtFe10DescPropertyGetterSymbol(getter, analysisContext)
        }

    override val javaGetterSymbol: KtFunctionSymbol
        get() = withValidityAssertion { KtFe10DescFunctionSymbol(descriptor.getterMethod, analysisContext) }

    override val javaSetterSymbol: KtFunctionSymbol?
        get() = withValidityAssertion {
            val setMethod = descriptor.setterMethod ?: return null
            return KtFe10DescFunctionSymbol(setMethod, analysisContext)
        }

    override val hasSetter: Boolean
        get() = withValidityAssertion { descriptor.setter != null }

    override val setter: KtPropertySetterSymbol?
        get() = withValidityAssertion {
            if (!descriptor.isVar) {
                return null
            }

            val setter = descriptor.setter ?: return KtFe10DescDefaultPropertySetterSymbol(descriptor, analysisContext)
            KtFe10DescPropertySetterSymbol(setter, analysisContext)
        }

    override val initializer: KtConstantValue?
        get() = withValidityAssertion { descriptor.compileTimeInitializer?.toKtConstantValue() }

    override val callableIdIfNonLocal: CallableId?
        get() = withValidityAssertion { descriptor.callableIdIfNotLocal }

    override val annotatedType: KtTypeAndAnnotations
        get() = withValidityAssertion { descriptor.type.toKtTypeAndAnnotations(analysisContext) }

    override val receiverType: KtTypeAndAnnotations?
        get() = withValidityAssertion { descriptor.extensionReceiverParameter?.type?.toKtTypeAndAnnotations(analysisContext) }

    override fun createPointer(): KtSymbolPointer<KtSyntheticJavaPropertySymbol> = withValidityAssertion {
        return KtPsiBasedSymbolPointer.createForSymbolFromSource(this) ?: KtFe10NeverRestoringSymbolPointer()
    }
}