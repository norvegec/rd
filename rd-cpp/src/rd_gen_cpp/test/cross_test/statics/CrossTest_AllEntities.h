#ifndef RD_CPP_CROSSTESTALLENTITIES_H
#define RD_CPP_CROSSTESTALLENTITIES_H


#include "DemoModel/DemoModel.h"
#include "ExtModel/ExtModel.h"

#include <string>
#include <vector>
#include <future>

namespace rd {
	namespace cross {
		class CrossTestAllEntities {
			using printer_t = std::vector<std::string>;
		public:
			static void fireAll(const demo::DemoModel& model, const demo::ExtModel& extModel);

		};
	}
}

#endif //RD_CPP_CROSSTESTALLENTITIES_H
