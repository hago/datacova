<script lang="ts">
import { computed, defineComponent, reactive, ref, type PropType } from 'vue';
import type { Task, TaskAction } from '@/entities/task/task';
import { identityStore } from '@/stores/identitystore';
import { isAdmin, isMaintainer } from '@/util/permission';
import workspaceApiHelper from '@/api/workspaceapi';
import { workspaceStore } from '@/stores/workspacestore';
import type { WorkspaceWithUser } from "@/api/workspaceapi";
import taskApiHelper from '@/api/taskapi';
import { EVENT_GLOBAL_DRAWER_NOTIFY, EVENT_REMOTE_API_ERROR, EVENT_TASKINFO_CLOSE_RECIPIENTS_EDITOR, EVENT_TASK_DELETED } from '@/entities/events';
import { eventBus } from '@/util/eventbus';
import RecipientsEditor from '../task/RecipientsEditor.vue';
import ActionIngest from '@/components/task/ActionIngest.vue';
import ActionDistribute from '@/components/task/ActionDistribute.vue';
import ActionVerify from '@/components/task/ActionVerify.vue';
import ActionTypeSelect from '@/components/task/ActionTypeSelect.vue'
import ActionBasicInfo from '@/components/task/ActionBasicInfo.vue'
import { buildSuccessDrawerConfig, type GlobalDrawerConfig } from '@/entities/globaldrawercfg';

export default defineComponent({
    name: "TaskInfo",
    components: {
        ActionBasicInfo, RecipientsEditor, ActionIngest, ActionDistribute, ActionVerify, ActionTypeSelect
    },
    props: {
        task: {
            type: Object as PropType<Task>,
            required: true
        }
    },
    setup(props) {
        const recipientsText = computed(() => {
            let n = 3
            let l = props.task.extra.mailRecipients.slice(0, n)
            if (l.length < n) {
                l = l.concat(props.task.extra.mailCCRecipients.slice(0, l.length - n))
                if (l.length < n) {
                    l = l.concat(props.task.extra.mailBCCRecipients.slice(0, l.length - n))
                }
            }
            return l.join(', ')
        })
        return reactive({
            canModify: false,
            canDelete: false,
            locales: [
                { label: '中文', value: 'zh_CN' },
                { label: 'English', value: 'en_US' }
            ],
            recipientsText,
            editRecipients: false
        })
    },
    mounted() {
        eventBus.register(EVENT_TASKINFO_CLOSE_RECIPIENTS_EDITOR, async (): Promise<any> => {
            console.log('close editrecp')
            this.editRecipients = false
            return Promise.resolve()
        })
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
                let user = identityStore().currentIdentity()
                workspaceApiHelper.getWorksapce(user, this.task.workspaceId).then(rsp => {
                    workspaceStore().setWorkspace(rsp.data)
                    this.updateTask(rsp.data)
                }).catch(err => {
                    eventBus.send(EVENT_REMOTE_API_ERROR, err)
                })
            }
        },
        updateTask(workspace: WorkspaceWithUser) {
            console.log(workspace)
            let user = identityStore().currentIdentity()
            this.canModify = isAdmin(user, workspace) || isMaintainer(user, workspace)
            this.canDelete = isAdmin(user, workspace)
        },
        deleteTask(task: Task) {
            if (!confirm(`Are you sure to delete the task "${task.name}"?`)) {
                return
            }
            let user = identityStore().currentIdentity()
            taskApiHelper.deleteTask(user, this.task).then(() => {
                this.$emit(EVENT_TASK_DELETED, task.id)
            }).catch((reason) => {
                eventBus.send(EVENT_REMOTE_API_ERROR, reason)
            })
        },
        isActionOfType(action: TaskAction, type: number): boolean {
            return action.type === type
        },
        addAction() {
            this.task.actions.push({
                type: -1,
                name: 'New Action',
                description: '',
                enabled: true
            })
        },
        shouldExpand(action: TaskAction) {
            return action.expand === undefined || action.expand
        },
        saveTask(task: Task) {
            let user = identityStore().currentIdentity()
            taskApiHelper.saveTask(user, task).then(rsp => {
                if (task.id <= 0) {
                    this.task.id = rsp.data.id
                }
                eventBus.send(EVENT_GLOBAL_DRAWER_NOTIFY, buildSuccessDrawerConfig(`Task ${rsp.data.name} saved`, "top"))
            }).catch(err => {
                eventBus.send(EVENT_REMOTE_API_ERROR, err)
            })
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span class="field">Name</span>
            <input class="taskname" v-model="task.name" type="text" placeholder="Task Name" v-bind:readonly="!canModify" />
        </n-gi>
        <n-gi style="text-align: right;">
            <n-button seconday type="primary" v-if="canDelete" @click="saveTask(task)" style="margin-right: 5px;">{{
                (task.id > 0) ? 'Update' : 'Save' }}</n-button>
            <n-button seconday type="error" v-if="canDelete" @click="deleteTask(task)">Delete</n-button>
        </n-gi>
        <n-gi :span="2">
            <n-card title="Description">
                <n-input v-model:value="task.description" type="textarea" placeholder="Task Description" />
            </n-card>
        </n-gi>
        <n-gi>
            <span class="field">Locale</span>
            <n-select :options="locales" v-model:value="task.extra.locale" readonly="!canModify" />
        </n-gi>
        <n-gi>
            <div class="field right">Updated: </div>
            <div class="updatetime right">{{ task.modifyTime > 0 ? new Date(task.modifyTime).toLocaleString() : "" }}</div>
        </n-gi>
        <n-gi span="2" style="margin-bottom: 5px">
            <div class="field">Notification recipients</div>
            <span>{{ recipientsText }}</span>
            <n-button seconday type="tertiary" @click="editRecipients = true" class="rightbutton">
                Edit Recipients
            </n-button>
        </n-gi>
        <n-gi class="actionheader">
            <div class="field">Task Actions</div>
        </n-gi>
        <n-gi class="actionheader">
            <n-button secondary type="warning" @click="addAction" class="rightbutton">
                Add New Action
            </n-button>
        </n-gi>
        <n-gi span="2" v-for="(act, index) in task.actions" v-bind:key="index">
            <ActionBasicInfo :action="act" :readonly="!canModify"> </ActionBasicInfo>
            <ActionTypeSelect :action="act" :task="task" :readonly="!canModify" v-if="shouldExpand(act)"></ActionTypeSelect>
            <ActionIngest :action="act" :task="task" v-if="isActionOfType(act, 1) && shouldExpand(act)"
                :readonly="!canModify">
            </ActionIngest>
            <ActionDistribute :action="act" :task="task" v-if="isActionOfType(act, 3) && shouldExpand(act)"
                :readonly="!canModify">
            </ActionDistribute>
            <ActionVerify :action="act" :task="task" v-if="isActionOfType(act, 2) && shouldExpand(act)"
                :readonly="!canModify">
            </ActionVerify>
            <div style="border-bottom-style: dotted"></div>
        </n-gi>
    </n-grid>
    <RecipientsEditor v-if="editRecipients" :extra="task.extra" :readonly="!canModify"></RecipientsEditor>
</template>

<style scoped>
.actionheader {
    margin-bottom: 5px;
    border-bottom-style: groove;
}

.taskname {
    font-weight: bold;
    font-size: larger;
    color: orange;
}

.field {
    font-family: Verdana, Geneva, Tahoma, sans-serif;
    font-size: large;
    color: ivory;
    padding-left: 5px;
    padding-right: 5px;
    margin-top: 3px;
}

.updatetime {
    padding-left: 5px;
    padding-right: 5px;
}

.right {
    text-align: right;
}

.rightbutton {
    float: right;
    width: 120px;
}
</style>
