<script lang="ts">
import type { WorkspaceConnection } from '@/entities/connection/workspaceconnection';
import { defineComponent, reactive, type PropType } from 'vue';
import MariaDbComponent from './dbconfig/MariaDbComponent.vue';
import PostgreSqlComponent from './dbconfig/PostgreSqlComponent.vue';
import MsSqlComponent from './dbconfig/MsSqlComponent.vue';
import connApiHelper from '@/api/connectionapi';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';

export default defineComponent({
    props: {
        connection: {
            type: Object as PropType<WorkspaceConnection>,
            required: true
        },
        editable: Boolean,
        deletable: Boolean
    },
    components: {
        MariaDbComponent, PostgreSqlComponent, MsSqlComponent
    },
    setup(props) {
        return reactive({
            dbTypeOptions: [
                { label: 'MariaDB / MySql', value: 'MariaDB' },
                { label: 'PostgreSQL', value: 'PostgreSQL' },
                { label: 'Microsoft SQL Sever', value: 'Microsoft SQL Sever' }
            ],
            dbNameOptions: [] as { label: string, value: string }[]
        })
    },
    methods: {
        saveConnection(conn: WorkspaceConnection) {

        },
        deleteconnection(conn: WorkspaceConnection) {

        },
        listDatabase() {
            let user = identityStore().currentIdentity()
            connApiHelper.verifyConnection(user, this.connection.configuration).then(rsp => {
                if (rsp.data.result) {
                    this.dbNameOptions = rsp.data.databases.map(d => ({ label: d, value: d }))
                } else {
                    eventBus.send(EVENT_REMOTE_API_ERROR, rsp.data.message)
                }
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
            <input class="taskname" v-model="connection.name" type="text" placeholder="Task Name"
                v-bind:readonly="!editable" />
        </n-gi>
        <n-gi style="text-align: right;">
            <n-button seconday type="primary" v-if="editable" @click="saveConnection(connection)"
                style="margin-right: 5px;">{{
                    (connection.id > 0) ? 'Update' : 'Save' }}</n-button>
            <n-button seconday type="error" v-if="deletable" @click="deleteconnection(connection)">Delete</n-button>
        </n-gi>
        <n-gi :span="2">
            <n-card title="Description">
                <n-input v-model:value="connection.description" type="textarea" placeholder="Connection Description" />
            </n-card>
        </n-gi>
        <n-gi>
            <div class="field">Data Source Type</div>
            <n-popselect :options="dbTypeOptions" v-model:value="connection.configuration.dbType">
                <n-button>{{ dbTypeOptions.find(i => i.value === connection.configuration.dbType)?.label }}</n-button>
            </n-popselect>
        </n-gi>
        <n-gi>
            <div class="field right">Updated: </div>
            <div class="updatetime right">{{ connection.modifyTime > 0 ? new Date(connection.modifyTime).toLocaleString() :
                "" }}</div>
        </n-gi>
        <n-gi span="2">
            <MariaDbComponent :config="connection.configuration" :editable="editable"
                v-if="connection.configuration.dbType === 'MariaDB'">
            </MariaDbComponent>
            <PostgreSqlComponent :config="connection.configuration" :editable="editable"
                v-if="connection.configuration.dbType === 'PostgreSQL'">
            </PostgreSqlComponent>
            <MsSqlComponent :config="connection.configuration" :editable="editable"
                v-if="connection.configuration.dbType === 'Microsoft SQL Sever'">
            </MsSqlComponent>
        </n-gi>
        <n-gi>
            <span style="margin-right: 5px;">Database</span>
            <n-popselect :options="dbNameOptions" v-model:value="connection.configuration.databaseName">
                <n-button>{{ connection.configuration.databaseName }}</n-button>
            </n-popselect>
        </n-gi>
        <n-gi>
            <n-button prinary class="rightbutton" @click="listDatabase">Refresh</n-button>
        </n-gi>
        <n-gi>
            <span>Login</span>
            <n-input type="text" v-model:value="connection.configuration.username" placeholder="user name"
                :disabled="!editable" ref="username"></n-input>
        </n-gi>
        <n-gi>
            <span>Password</span>
            <n-input type="password" v-model:value="connection.configuration.password" placeholder="Password"
                :disabled="!editable" ref="password"></n-input>
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
