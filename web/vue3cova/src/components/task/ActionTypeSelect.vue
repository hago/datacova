<script lang="ts">
import type { Task, TaskAction } from '@/entities/task/task';
import { computed, defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        action: {
            type: Object as PropType<TaskAction>,
            required: true
        },
        task: {
            type: Object as PropType<Task>,
            required: true
        },
        readonly: {
            type: Boolean,
            required: true
        }
    },
    setup(props) {
        let typeOptions = [{
            label: "Ingestion",
            value: 1
        }, {
            label: "Verification",
            value: 2
        }, {
            label: "distribution",
            value: 3
        }]
        return reactive({
            typeOptions,
            selectedType: computed(() => {
                if (props.action.type === -1) {
                    return ""
                } else {
                    let t = typeOptions.find(i => i.value === props.action.type)
                    return t === undefined ? "" : t.label
                }
            }),
            showSelect: props.action.type < 0
        })
    },
    methods: {
        changeActionType() {
            if (confirm("Are you SURE to change the existing action type??")) {
                this.showSelect = true
            }
        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi span="2">
            <span class="actionheader">Action:&nbsp;</span>
            <span class="actionname">{{ action.name }}</span>
        </n-gi>
        <n-gi style="margin-right: 10px;">
            <span class="field">Type: </span>
            <n-gradient-text type="info">{{ selectedType }}</n-gradient-text>
            <n-button quaternary type="warning" @click="changeActionType" style="float: right;">Change Type</n-button>
        </n-gi>
        <n-gi>
            <n-select :options="typeOptions" v-model:value="action.type" v-if="showSelect" :readonly="readonly"></n-select>
        </n-gi>
    </n-grid>
</template>

<style scoped>
.actionheader {
    color: cornflowerblue;
    font-size: large;
}

.actionname {
    color: yellow;
    font-size: large;
}
</style>
