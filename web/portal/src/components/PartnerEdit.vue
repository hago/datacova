<template>
  <div style="padding:10px">
    <div class="form-row form-group">
      <label for="parterName" class="col-1">Name</label>
      <div class="form-group col">
        <input
          type="text"
          placeholder="Partner name"
          id="parterName"
          v-model="partner.name"
          size="100"
          class="form-control" />
      </div>
    </div>
    <div class="form-row form-group">
      <label for="parterDescription" class="col-1">Description</label>
      <div class="form-group col">
        <textarea
          placeholder="Partner Description"
          id="parterDescription"
          v-model="partner.description"
          class="form-control" />
      </div>
    </div>
    <div class="form-row form-group">
      <label for="onStartUrl" class="col-1">On Start Callback URL</label>
      <div class="form-group col">
        <input
          type="text"
          placeholder="http://host/notify/start"
          id="onStartUrl"
          v-model="partner.callback.onStartUrl"
          size="100"
          class="form-control" />
      </div>
    </div>
    <div class="form-row form-group">
      <label for="onProgressUrl" class="col-1">On Progress Callback URL</label>
      <div class="form-group col">
        <input
          type="text"
          placeholder="http://host/notify/progress"
          id="onProgressUrl"
          v-model="partner.callback.onProgressUrl"
          size="100"
          class="form-control" />
      </div>
    </div>
    <div class="form-row form-group">
      <label for="onCompleteUrl" class="col-1">On Complete Callback URL</label>
      <div class="form-group col">
        <input
          type="text"
          placeholder="http://host/notify/complete"
          id="onCompleteUrl"
          v-model="partner.callback.onCompleteUrl"
          size="100"
          class="form-control" />
      </div>
    </div>
    <div class="form-row ">
      <div class="col">
        <label for="parterId">Partner ID</label>
        <div class="form-group">
          <input type="text" readonly
            placeholder="will be generated automatically"
            id="parterId"
            v-model="partner.partnerId"
            class="form-control" />
        </div>
      </div>
      <div class="col">
        <label for="parterId">Partner Key</label>
        <div class="form-group">
          <input type="text" readonly
            placeholder="will be generated automatically"
            id="parterKey"
            v-model="partner.partnerKey"
            class="form-control" />
        </div>
      </div>
    </div>
    <div class="form-row">
      <button class="btn btn-primary col-1" style="margin-right: 10px" v-on:click="getIdKey()">Generate ID/Key</button>
      <button class="btn btn-info col-1" v-on:click="save()">Save</button>
    </div>
  </div>
</template>

<script>
import PartnerApiHelper from '../apis/partnerapi'
import Toasted from 'vue-toasted'
import Vue from 'vue'

Vue.use(Toasted, {
  duration: 1000,
  position: 'center'
})

export default {
  name: 'PartnerEdit',
  data () {
    return {
      partner: this.emptyPartner()
    }
  },
  methods: {
    emptyPartner: function () {
      return {
        id: 0,
        name: '',
        description: '',
        partnerId: '',
        partnerKey: '',
        info: {},
        status: 0,
        callback: {
          onStartUrl: null,
          onProgressUrl: null,
          onCompleteUrl: null
        }
      }
    },
    getIdKey: function () {
      (new PartnerApiHelper(process.env.SERVICE_BASE_URL)).getIdKey()
        .then((result) => {
          this.partner.partnerId = result.data.data.id
          this.partner.partnerKey = result.data.data.key
        })
        .catch((err) => {
          Vue.toasted.error(err)
        })
    },
    save: function () {
      (new PartnerApiHelper(process.env.SERVICE_BASE_URL)).savePartner(this.partner)
        .then((result) => {
          this.partner = result.data.data.partner
        })
        .catch((err) => {
          Vue.toasted.error(err).goAway(2000)
        })
    }
  },
  created: function () {
    let id = this.$route.params.id
    if (parseInt(id) === -1) {
      return
    }
    (new PartnerApiHelper(process.env.SERVICE_BASE_URL)).loadPartner(id)
      .then((result) => {
        this.partner = result.data.data.partner
      }).catch((err) => {
        Vue.toasted.error(err)
      })
  }
}
</script>

<style scoped>
.invalid {
  border:2px solid red
}
</style>
