<script lang="ts">
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { defineComponent, reactive, type PropType } from 'vue';

export default defineComponent({
    props: {
        connection: {
            type: Object as PropType<WorkspaceConnection>,
            required: true
        },
        editable: Boolean,
        deletable: Boolean
    },
    setup(props) {
        return reactive({})
    },
    methods: {
        saveConnection(conn: WorkspaceConnection) {

        },
        deleteconnection(conn: WorkspaceConnection) {

        }
    }
})
</script>

<template>
    <n-grid cols="2">
        <n-gi>
            <span class="field">Name</span>
            <input class="taskname" v-model="connection.name" type="text" placeholder="Task Name" v-bind:readonly="!editable" />
        </n-gi>
        <n-gi style="text-align: right;">
            <n-button seconday type="primary" v-if="editable" @click="saveConnection(connection)" style="margin-right: 5px;">{{
                (connection.id > 0) ? 'Update' : 'Save' }}</n-button>
            <n-button seconday type="error" v-if="deletable" @click="deleteconnection(connection)">Delete</n-button>
        </n-gi>
    </n-grid>
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
