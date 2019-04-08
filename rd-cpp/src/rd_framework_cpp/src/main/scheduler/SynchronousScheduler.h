//
// Created by jetbrains on 4/7/2019.
//

#ifndef RD_CPP_SYNCHRONOUSSCHEDULER_H
#define RD_CPP_SYNCHRONOUSSCHEDULER_H

#include "IScheduler.h"

#include "guards.h"

namespace rd {
	class SynchronousScheduler : public IScheduler {
		static thread_local int32_t active;
	public:
		//region ctor/dtor

		SynchronousScheduler() = default;

		SynchronousScheduler(SynchronousScheduler const &) = delete;

		SynchronousScheduler(SynchronousScheduler &&) = delete;

		virtual ~SynchronousScheduler() = default;
		//endregion

		void queue(std::function<void()> action) override {
			util::increment_guard<int32_t> guard(active);
			action();
		}

		void flush() override {

		}

		bool is_active() const override {
			return active > 0;
		}
	};

	extern SynchronousScheduler globalSynchronousScheduler;
}


#endif //RD_CPP_SYNCHRONOUSSCHEDULER_H