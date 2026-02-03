package com.qinglan.example.device_point.server.handle;

import com.qinglan.example.device_point.server.msg.ServerLBSInfo;
import com.qinglan.example.device_point.server.session.DeviceRegSession;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@ChannelHandler.Sharable
@Slf4j
public class PositionEventHandler extends SimpleChannelInboundHandler<ServerLBSInfo.PositionStatusEvent> {

    /**
     * 轨迹事件
     * Trajectory event
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ServerLBSInfo.PositionStatusEvent msg) throws Exception {
        try {
            String uid = DeviceRegSession.getUidByChannelId(ctx.channel().id());
            byte[] event = msg.getEvents().toByteArray();
            byte[] area = msg.getAreas().toByteArray();
            log.info("-----uid:{}--------event:{}----area: {}------", uid, Arrays.toString(event), Arrays.toString(area));
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

}
