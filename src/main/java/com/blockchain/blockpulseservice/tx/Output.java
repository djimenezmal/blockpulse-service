package com.blockchain.blockpulseservice.tx;

import com.fasterxml.jackson.annotation.JsonProperty;

record Output (@JsonProperty("value") long value){}