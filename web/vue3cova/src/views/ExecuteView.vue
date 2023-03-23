<script lang="ts">
import taskApiHelper from '@/api/taskapi';
import { EVENT_REMOTE_API_ERROR } from '@/entities/events';
import type { Task } from '@/entities/task/task';
import { identityStore } from '@/stores/identitystore';
import { eventBus } from '@/util/eventbus';
import { defineComponent, reactive } from 'vue';
import { darkTheme, type UploadCustomRequestOptions } from "naive-ui";
import ExcelExtra from '@/components/task/upload/ExcelExtra.vue';
import CsvExtra from '@/components/task/upload/CsvExtra.vue';
import ParquetExtra from '@/components/task/upload/ParquetExtra.vue';
import UnsupportedUpload from '@/components/task/upload/UnsupportedUpload.vue';
import type { BaseFileInfo } from '@/entities/datafile/BaseFileInfo';

export default defineComponent({
    components: { ExcelExtra, CsvExtra, ParquetExtra, UnsupportedUpload },
    setup() {
        return reactive({
            task: null as Task | null,
            darkTheme,
            columns: [] as { title: string, key: string }[],
            data: [] as any[],
            uploadOptions: null as UploadCustomRequestOptions | null,
            fileType: undefined as 'Excel' | 'CSV' | 'Parquet' | null | undefined,
            fileInfo: {
                filename: '',
                type: 0
            } as BaseFileInfo
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
    },
    methods: {
        selectFile(uploadCustomRequestOptions: UploadCustomRequestOptions) {
            this.uploadOptions = uploadCustomRequestOptions
            let ext = this.uploadOptions.file.name.toLocaleLowerCase()
            if (ext.endsWith('.xls') || ext.endsWith('.xlsx')) {
                this.fileType = 'Excel'
            } else if (ext.endsWith('csv')) {
                this.fileType = 'CSV'
            } else {
                this.fileType = null
            }
        },
        preview() {
            let user = identityStore().currentIdentity()
            taskApiHelper.previewFile(user, this.uploadOptions!.file.file!, this.fileInfo).then(rsp => {
                this.columns = rsp.data.columns.map(col => ({ title: col, key: col }))
                this.data = rsp.data.rows.map(row => {
                    let r = {} as {
                        [key: string]: any
                    }
                    for (let i = 0; i < rsp.data.columns.length; i++) {
                        r[this.columns[i].title] = row[i]
                    }
                    return r
                })
            }).catch(err => {
                eventBus.send(EVENT_REMOTE_API_ERROR, err)
            })
        }
    }
})
</script>

<template>
    <n-config-provider :theme="darkTheme">
        <n-grid cols="6" v-if="task !== null">
            <n-gi></n-gi>
            <n-gi span="4">
                <h1 style="color: gold;">{{ task.name }}</h1>
                <div>{{ task.description }}</div>
                <div style="color: green">Actions</div>
                <ol class="actionlist">
                    <li v-for="(action, i) in task.actions" :key="i">
                        <div class>{{ action.name }}</div>
                    </li>
                </ol>
            </n-gi>
            <n-gi></n-gi>
            <n-gi></n-gi>
            <n-gi span="4">
                <n-input-group>
                    <n-upload :custom-request="selectFile" :show-file-list="false">
                        <n-button>Select File</n-button>
                    </n-upload>
                    <n-button v-if="uploadOptions !== null" @click="preview">Preview</n-button>
                </n-input-group>
            </n-gi>
            <n-gi></n-gi>
            <n-gi></n-gi>
            <n-gi span="4">
                <ExcelExtra :fileInfo="fileInfo" v-if="fileType === 'Excel'"></ExcelExtra>
                <CsvExtra :fileInfo="fileInfo" v-if="fileType === 'CSV'"></CsvExtra>
                <ParquetExtra :fileInfo="fileInfo" v-if="fileType === 'Parquet'"></ParquetExtra>
                <UnsupportedUpload :fileInfo="fileInfo" v-if="fileType === null"></UnsupportedUpload>
            </n-gi>
            <n-gi></n-gi>
            <n-gi></n-gi>
            <n-gi span="4">
                <n-data-table :columns="columns" :data="data" :bordered="false" v-if="data.length > 0" />
            </n-gi>
            <n-gi></n-gi>
        </n-grid>
    </n-config-provider>
</template>

<style scoped>
.actionlist {
    color: cyan;
    font-size: large;
}

.actionlist li {
    color: green
}
</style>
