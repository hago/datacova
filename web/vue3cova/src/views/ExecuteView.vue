<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { Task } from '@/entities/task/task';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive } from 'vue';

export default defineComponent({
    setup() {
        return reactive({
            task: null as Task | null
        })
    },
    mounted() {
        //console.log(this.$route.params.workspaceid, this.$route.params.id)
        let workspaceid = parseInt(this.$route.params.workspaceid as string)
        let taskId = parseInt(this.$route.params.id as string)
        taskApiHelper.getTask(identityStore().currentIdentity(), workspaceid, taskId).then(rsp => {
            this.task = rsp.data
        }).catch(err => {
            eventBus.send(EVENT_REMOTE_API_ERROR, err)
        })
    }
})
</script>

<template>
    <n-space>Execute</n-space>
</template>