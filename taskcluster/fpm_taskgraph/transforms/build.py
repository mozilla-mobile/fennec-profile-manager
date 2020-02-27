# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the jobs defined in the build
kind.
"""

from __future__ import absolute_import, print_function, unicode_literals

import datetime

from taskgraph.transforms.base import TransformSequence
from fpm_taskgraph.gradle import get_variant
from fpm_taskgraph.util import upper_case_first_letter


transforms = TransformSequence()


@transforms.add
def add_variant_config(config, tasks):
    for task in tasks:
        attributes = task.setdefault("attributes", {})
        if not attributes.get("build-type"):
            attributes["build-type"] = task["name"]

        yield task


@transforms.add
def build_gradle_command(config, tasks):
    for task in tasks:
        gradle_build_type = task["run"]["gradle-build-type"]
        gradle_flavor = task["run"]["gradle-flavor"]
        variant_config = get_variant(gradle_build_type, gradle_flavor)

        task["run"]["gradlew"] = [
            "clean",
            "assemble{}".format(upper_case_first_letter(variant_config["name"]))
        ]

        yield task


@transforms.add
def add_artifacts(config, tasks):
    for task in tasks:
        gradle_build_type = task["run"].pop("gradle-build-type")
        gradle_flavor = task["run"].pop("gradle-flavor")
        variant_config = get_variant(gradle_build_type, gradle_flavor)
        artifacts = task.setdefault("worker", {}).setdefault("artifacts", [])
        task["attributes"]["apks"] = apks = {}

        if "apk-artifact-template" in task:
            artifact_template = task.pop("apk-artifact-template")
            for apk in variant_config["apks"]:
                apk_name = artifact_template["name"].format(
                    gradle_flavor=gradle_flavor, **apk
                )
                artifacts.append({
                    "type": artifact_template["type"],
                    "name": apk_name,
                    "path": artifact_template["path"].format(
                        gradle_flavor=gradle_flavor,
                        gradle_build_type=gradle_build_type,
                        **apk
                    ),
                })
                apks[apk["abi"]] = apk_name

        yield task
