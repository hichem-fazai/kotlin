/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.backend.jvm

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.backend.common.lower.*
import org.jetbrains.kotlin.backend.common.phaser.*
import org.jetbrains.kotlin.backend.jvm.lower.*
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.PatchDeclarationParentsVisitor
import org.jetbrains.kotlin.ir.visitors.acceptVoid

private fun makePatchParentsPhase(number: Int) = namedIrFilePhase(
    lower = object : SameTypeCompilerPhase<CommonBackendContext, IrFile> {
        override fun invoke(phaseConfig: PhaseConfig, phaserState: PhaserState, context: CommonBackendContext, input: IrFile): IrFile {
            input.acceptVoid(PatchDeclarationParentsVisitor())
            return input
        }
    },
    name = "PatchParents$number",
    description = "Patch parent references in IrFile, pass $number",
    nlevels = 0
)

internal val jvmPhases = namedIrFilePhase(
    name = "IrLowering",
    description = "IR lowering",
    lower = jvmCoercionToUnitPhase then
            fileClassPhase then
            kCallableNamePropertyPhase then

            jvmLateinitPhase then

            moveCompanionObjectFieldsPhase then
            constAndJvmFieldPropertiesPhase then
            propertiesPhase then
            annotationPhase then

            jvmDefaultArgumentStubPhase then

            interfacePhase then
            interfaceDelegationPhase then
            sharedVariablesPhase then

            makePatchParentsPhase(1) then

            jvmLocalDeclarationsPhase then
            callableReferencePhase then
            functionNVarargInvokePhase then

            innerClassesPhase then
            innerClassConstructorCallsPhase then

            makePatchParentsPhase(2) then

            enumClassPhase then
            objectClassPhase then
            makeInitializersPhase(JvmLoweredDeclarationOrigin.CLASS_STATIC_INITIALIZER, true) then
            singletonReferencesPhase then
            syntheticAccessorPhase then
            bridgePhase then
            jvmOverloadsAnnotationPhase then
            jvmStaticAnnotationPhase then
            staticDefaultFunctionPhase then

            tailrecPhase then
            toArrayPhase then

            makePatchParentsPhase(3)
)

class JvmLower(val context: JvmBackendContext) {
    fun lower(irFile: IrFile) {
        // TODO run lowering passes as callbacks in bottom-up visitor
        jvmPhases.invokeToplevel(context.phaseConfig, context, irFile)
    }
}
