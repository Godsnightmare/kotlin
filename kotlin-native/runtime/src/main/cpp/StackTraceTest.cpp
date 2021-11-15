/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#include "StackTrace.hpp"

#include <signal.h>

#include "gmock/gmock.h"
#include "gtest/gtest.h"

#include "Common.h"
#include "Porting.h"
#include "TestSupport.hpp"

#include <iostream>

using namespace kotlin;

namespace {

template <size_t Capacity = kotlin::kDynamicCapacity>
NO_INLINE StackTrace<Capacity> GetStackTrace1(int skipFrames = 0) {
    return StackTrace<Capacity>::current(skipFrames);
}

template <size_t Capacity = kotlin::kDynamicCapacity>
NO_INLINE StackTrace<Capacity> GetStackTrace2(int skipFrames = 0) {
    return GetStackTrace1<Capacity>(skipFrames);
}

template <size_t Capacity = kotlin::kDynamicCapacity>
NO_INLINE StackTrace<Capacity> GetStackTrace3(int skipFrames = 0) {
    return GetStackTrace2<Capacity>(skipFrames);
}

// TODO: Deep stacktrace.

NO_INLINE void AbortWithStackTrace(int) {
    PrintStackTraceStderr();
    konan::abort();
}

} // namespace

TEST(StackTraceTest, StackTrace) {
    auto stackTrace = GetStackTrace3();
    auto symbolicStackTrace = GetStackTraceStrings(stackTrace.data());
    ASSERT_GT(symbolicStackTrace.size(), 0ul);
    EXPECT_THAT(symbolicStackTrace[0], testing::HasSubstr("GetStackTrace1"));
    EXPECT_THAT(symbolicStackTrace[1], testing::HasSubstr("GetStackTrace2"));
}

TEST(StackTraceTest, StackTraceWithSkip) {
    constexpr int kSkip = 1;
    auto stackTrace = GetStackTrace3(kSkip);
    auto symbolicStackTrace = GetStackTraceStrings(stackTrace.data());
    ASSERT_GT(symbolicStackTrace.size(), 0ul);
    EXPECT_THAT(symbolicStackTrace[0], testing::HasSubstr("GetStackTrace2"));
    EXPECT_THAT(symbolicStackTrace[1], testing::HasSubstr("GetStackTrace3"));
}

TEST(StackTraceTest, StackAllocatedTrace) {
    auto stackTrace = GetStackTrace3<2>();
    auto symbolicStackTrace = GetStackTraceStrings(stackTrace.data());
    ASSERT_EQ(symbolicStackTrace.size(), 2ul);
    EXPECT_THAT(symbolicStackTrace[0], testing::HasSubstr("GetStackTrace1"));
    EXPECT_THAT(symbolicStackTrace[1], testing::HasSubstr("GetStackTrace2"));
}

TEST(StackTraceTest, StackAllocatedTraceWithSkip) {
    constexpr int kSkip = 1;
    auto stackTrace = GetStackTrace3<2>(kSkip);
    auto symbolicStackTrace = GetStackTraceStrings(stackTrace.data());
    ASSERT_EQ(symbolicStackTrace.size(), 2ul);
    EXPECT_THAT(symbolicStackTrace[0], testing::HasSubstr("GetStackTrace2"));
    EXPECT_THAT(symbolicStackTrace[1], testing::HasSubstr("GetStackTrace3"));
}

/*
 *
 * TODO:
 *  - Empty trace
 *  - Empty trace + stack allocation.
 *  - Deep trace
 *
 *
 */


TEST(StackTraceDeathTest, PrintStackTrace) {
    EXPECT_DEATH(
            { AbortWithStackTrace(0); },
            testing::AllOf(
                    testing::HasSubstr("AbortWithStackTrace"), testing::HasSubstr("StackTraceDeathTest_PrintStackTrace_Test"),
                    testing::Not(testing::HasSubstr("PrintStackTraceStderr"))));
}

TEST(StackTraceDeathTest, PrintStackTraceInSignalHandler) {
    EXPECT_DEATH(
            {
                signal(SIGINT, &AbortWithStackTrace);
                raise(SIGINT);
            },
            testing::AllOf(
                    testing::HasSubstr("AbortWithStackTrace"),
                    testing::HasSubstr("StackTraceDeathTest_PrintStackTraceInSignalHandler_Test"),
                    testing::Not(testing::HasSubstr("PrintStackTraceStderr"))));
}
