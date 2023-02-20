<script lang="ts">
import { defineComponent, reactive, type PropType } from 'vue';
import type { Task } from '@/entities/task/task';
import { currentIdentity, identityStore } from '@/stores/identitystore';
import { isAdmin, isMaintainer } from '@/util/permission';
import workspaceApiHelper from '@/api/workspaceapi';
import { workspaceStore } from '@/stores/workspacestore';
import type { WorkspaceWithUser } from "@/api/workspaceapi";
import taskApiHelper from '@/api/taskapi';
import { EVENT_REMOTE_API_ERROR, EVENT_TASK_DELETED } from '@/entities/events';

export default defineComponent({
    name: "TaskInfo",
    props: {
        task: {
            type: Object as PropType<Task>,
            required: true
        }
    },
    setup() {
        return reactive({
            canModify: false,
            canDelete: false
        })
    },
    mounted() {
        this.updateWorkspaceTask()
    },
    update() {
        this.updateWorkspaceTask()
    },
    methods: {
        updateWorkspaceTask() {
            let w = workspaceStore().getWorkspace(this.task.id)
            if (w !== null) {
                this.updateTask(w)
            } else {
                let user = currentIdentity(identityStore())
                workspaceApiHelper.getWorksapce(user, this.task.workspaceId, {
                    success: (rsp) => {
                        workspaceStore().setWorkspace(rsp.data)
                        this.updateTask(rsp.data)
                    },
                    fail: () => { }
                })
            }
        },
        updateTask(workspace: WorkspaceWithUser) {
            console.log(workspace)
            let user = currentIdentity(identityStore())
            this.canModify = isAdmin(user, workspace) || isMaintainer(user, workspace)
            this.canDelete = isAdmin(user, workspace)
        },
        deleteTask(task: Task) {
            if (!confirm(`Are you sure to delete the task "${task.name}"?`)) {
                return
            }
            let user = currentIdentity(identityStore())
            taskApiHelper.deleteTask(user, this.task).then(() => {
                this.$emit(EVENT_TASK_DELETED, task.id)
            }).catch((reason) => {
                this.$emit(EVENT_REMOTE_API_ERROR, reason)
            })
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <input class="taskname" v-model="task.name" type="text" placeholder="Task Name" v-bind:readonly="!canModify" />
        </n-gi>
        <n-gi style="text-align: right;">
            <n-button type="error" v-if="canDelete" @click="deleteTask(task)">Delete</n-button>
        </n-gi>
        <n-gi>
            <h2>{{ `Workspace ${task?.name}` }}</h2>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.taskname {
    font-weight: bold;
    font-size: larger;
    color: orange;
}
</style>
