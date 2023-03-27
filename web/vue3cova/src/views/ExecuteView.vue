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
import uploadApiHelper, { type UploadedFileInfo } from '@/api/uploadapi';
import { FILE_TYPE_CSV } from '@/entities/datafile/CsvFileInfo';
import { FILE_TYPE_EXCEL } from '@/entities/datafile/ExcelFileInfo';
import { FILE_TYPE_EXCEL_OOXML } from '@/entities/datafile/ExcelXFileInfo';
import { FILE_TYPE_PARQUET } from '@/entities/datafile/ParquetFileInfo';

export default defineComponent({
    components: { ExcelExtra, CsvExtra, ParquetExtra, UnsupportedUpload },
    setup() {
        return reactive({
            task: null as Task | null,
            darkTheme,
            columns: [] as { title: string, key: string }[],
            data: [] as any[],
            uploaded: null as null | UploadedFileInfo,
            FILE_TYPE_CSV,
            FILE_TYPE_EXCEL,
            FILE_TYPE_EXCEL_OOXML,
            FILE_TYPE_PARQUET
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
        uploadFile(uploadCustomRequestOptions: UploadCustomRequestOptions) {
            uploadApiHelper.uploadFile(identityStore().currentIdentity(), uploadCustomRequestOptions.file.file!).then(rsp => {
                this.uploaded = rsp.data[0]
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
                    <n-upload :custom-request="uploadFile" :show-file-list="false">
                        <n-button>Upload File</n-button>
                    </n-upload>
                    <n-button v-if="uploaded !== null">Preview</n-button>
                </n-input-group>
            </n-gi>
            <n-gi></n-gi>
            <n-gi></n-gi>
            <n-gi span="4">
                <ExcelExtra :fileInfo="uploaded"
                    v-if="uploaded?.type === FILE_TYPE_EXCEL || uploaded?.type === FILE_TYPE_EXCEL_OOXML"></ExcelExtra>
                <CsvExtra :fileInfo="uploaded" v-else-if="uploaded?.type === FILE_TYPE_CSV"></CsvExtra>
                <ParquetExtra :fileInfo="uploaded" v-else-if="uploaded?.type === FILE_TYPE_PARQUET"></ParquetExtra>
                <UnsupportedUpload :fileInfo="uploaded" v-else></UnsupportedUpload>
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
