package com.softlabs.aicontents.domain.publish.mapper;

import java.nio.channels.Channel;
import java.util.List;

public interface ChannelMapper {
    List<Channel> selectbyIds(List<Long> channelIds);
    Channel selectbyId(Long channelId);
}