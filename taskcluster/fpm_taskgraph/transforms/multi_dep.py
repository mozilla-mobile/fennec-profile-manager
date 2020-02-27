# This Source Code Form is subject to the terms of the Mozilla Public
# License, v. 2.0. If a copy of the MPL was not distributed with this
# file, You can obtain one at http://mozilla.org/MPL/2.0/.
"""
Apply some defaults and minor modifications to the single_dep jobs.
"""

from __future__ import absolute_import, print_function, unicode_literals

from taskgraph.transforms.base import TransformSequence
from taskgraph.util.schema import resolve_keyed_by
from taskgraph.util.treeherder import inherit_treeherder_from_dep, join_symbol


transforms = TransformSequence()


@transforms.add
def build_name_and_attributes(config, tasks):
    for task in tasks:
        task["dependencies"] = {
            dep_key: dep.label
            for dep_key, dep in _get_all_deps(task).iteritems()
        }
        primary_dep = task.pop("primary-dependency")
        copy_of_attributes = primary_dep.attributes.copy()
        task.setdefault("attributes", copy_of_attributes)
        # run_on_tasks_for is set as an attribute later in the pipeline
        task.setdefault("run-on-tasks-for", copy_of_attributes['run_on_tasks_for'])
        task["name"] = _get_dependent_job_name_without_its_kind(primary_dep)

        yield task


def _get_dependent_job_name_without_its_kind(dependent_job):
    return dependent_job.label[len(dependent_job.kind) + 1:]


def _get_all_deps(task):
    if task.get("dependent-tasks"):
        return task["dependent-tasks"]

    return {task["primary-dependency"].kind: task["primary-dependency"]}


@transforms.add
def build_upstream_artifacts(config, tasks):
    for task in tasks:
        worker_definition = {
            "upstream-artifacts": [],
        }

        for dep in _get_all_deps(task).values():
            paths = sorted(dep.attributes.get("apks", {}).values())
            if paths:
                worker_definition["upstream-artifacts"].append({
                    "taskId": {"task-reference": "<{}>".format(dep.kind)},
                    "taskType": dep.kind,
                    "paths": paths,
                })

        task["worker"].update(worker_definition)
        yield task


@transforms.add
def remove_dependent_tasks(config, tasks):
    for task in tasks:
        try:
            del task["dependent-tasks"]
        except KeyError:
            pass

        yield task
