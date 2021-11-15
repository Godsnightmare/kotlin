/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

#ifndef RUNTIME_STACK_TRACE_H
#define RUNTIME_STACK_TRACE_H

#include "cpp_support/Span.hpp"
#include "Memory.h"
#include "Types.h"

namespace kotlin {

namespace internal {

NO_INLINE KStdVector<void*> GetCurrentStackTrace(size_t skipFrames) noexcept;
NO_INLINE size_t GetCurrentStackTrace(size_t skipFrames, std_support::span<void*> buffer) noexcept;

}

static constexpr size_t kDynamicCapacity = std::numeric_limits<size_t>::max();

// TODO: Instead of KStd* provide allocator-customizable versions, to allow stack memory allocation.
// TODO: Model API as in upcoming https://en.cppreference.com/w/cpp/utility/basic_stacktrace
template <size_t Capacity = kDynamicCapacity>
class StackTrace {
    // TODO: Add an iterator.
    // TODO: Add tests from Sasha's branch
    // TODO: Deal with size_t and ints

public:
    StackTrace() noexcept : size_(0), buffer_{nullptr} {};
    StackTrace(const StackTrace<Capacity>& other) = default;
    StackTrace(StackTrace<Capacity>&& other) noexcept = default;

    StackTrace& operator=(const StackTrace<Capacity>& other) = default;
    StackTrace& operator=(StackTrace<Capacity>&& other) noexcept = default;

    size_t size() noexcept { return size_; }

    void*& operator[](size_t index) { return buffer_[index]; }

    std_support::span<void*> data() noexcept {
        return std_support::span<void*>(buffer_.data(), size());
    }

    NO_INLINE static StackTrace<Capacity> current(size_t skipFrames = 0) {
        StackTrace result;
        result.size_ = internal::GetCurrentStackTrace(
                skipFrames + 1, std_support::span<void*>(result.buffer_.data(), result.buffer_.size()));
        return result;
    }

private:
    size_t size_;
    // TODO: Should we increase the capacity under the hood to take into account the frames added by current() and GetCurrentStackTrace()?
    std::array<void*, Capacity> buffer_;
};

template<>
class StackTrace<kDynamicCapacity> {
public:
    StackTrace() noexcept = default;
    StackTrace(const StackTrace<kDynamicCapacity>& other) = default;
    StackTrace(StackTrace<kDynamicCapacity>&& other) noexcept = default;

    StackTrace& operator=(const StackTrace<kDynamicCapacity>& other) noexcept = default;
    StackTrace& operator=(StackTrace<kDynamicCapacity>&& other) noexcept = default;

    size_t size() noexcept {
        return buffer_.size();
    }

    void*& operator[](size_t index) {
        return buffer_[index];
    }

    std_support::span<void*> data() noexcept {
        return std_support::span<void*>(buffer_.data(), size());
    }

    NO_INLINE static StackTrace<kDynamicCapacity> current(size_t skipFrames = 0) {
        StackTrace result;
        result.buffer_ = internal::GetCurrentStackTrace(skipFrames + 1);
        return result;
    }

private:
    KStdVector<void*> buffer_;
};


KStdVector<KStdString> GetStackTraceStrings(const std_support::span<void* const> stackTrace) noexcept;

// It's not always safe to extract SourceInfo during unhandled exception termination.
void DisallowSourceInfo();

void PrintStackTraceStderr();

} // namespace kotlin

#endif // RUNTIME_STACK_TRACE_H