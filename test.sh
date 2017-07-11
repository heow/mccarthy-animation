#!/bin/sh
#lein cljsbuild test
#planck ./mccarthy_animation/core_test.cljs
pushd ./src
planck ./mccarthy_animation/character_test.cljs
popd
