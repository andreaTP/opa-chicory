package example.one

import data.example.one.myCompositeRule

default myRule = false

default myOtherRule = false

myRule {
	input.someProp == "thisValue"
}

myOtherRule {
	input.anotherProp == "thatValue"
}
