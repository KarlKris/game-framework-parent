package com.echo.ramcache;

import com.echo.common.id.MultiServerIdGenerator;
import com.echo.engine.boostrap.NettyServerBootstrap;
import com.echo.ioc.context.AnnotationGenericApplicationContext;
import com.echo.mongo.core.MongoTemplate;
import com.echo.ramcache.entity.*;
import org.junit.Test;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: li-yuanwen
 */
public class RamcacheTest {

    @Test
    public void insertTest() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        MongoTemplate template = context.getBean(MongoTemplate.class);
        MultiServerIdGenerator generator = new MultiServerIdGenerator(1);
        List<Item> itemList = new ArrayList<>(100);
        List<Player> playerList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long id = generator.nextId();
            Player player = new Player(id, "name-" + i + 1);
            playerList.add(player);
            for (int j = 0; j < 10; j++) {
                Item item = new Item(generator.nextId(), id, j + 1);
                itemList.add(item);
            }
        }
        template.insertAll(playerList);
        template.insertAll(itemList);
    }


    @Test
    public void ramcacheTest() {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        long id = 148674469756929L;

        EntityCacheService entityCacheService = context.getBean(EntityCacheService.class);
        Player player = entityCacheService.loadEntity(id, Player.class);

        RegionEntityCacheService regionEntityCacheService = context.getBean(RegionEntityCacheService.class);
        PlayerItems playerItems = regionEntityCacheService.loadRegionContext(id, Item.class, PlayerItems::new);

        Player player2 = entityCacheService.loadEntity(id, Player.class);
        playerItems = regionEntityCacheService.loadRegionContext(id, Item.class, PlayerItems::new);

        assert player.getId() == id;

    }

    public static void main(String[] args) throws SocketException, InterruptedException {
        AnnotationGenericApplicationContext context = new AnnotationGenericApplicationContext("com.echo");
        context.refresh();

        NettyServerBootstrap bootstrap = context.getBean(NettyServerBootstrap.class);
        bootstrap.startServer();

        long id = 148742103891969L;

        EntityCacheService entityCacheService = context.getBean(EntityCacheService.class);
        Player player = entityCacheService.loadEntity(id, Player.class);
        player.modify("test-commit suc");

        RegionEntityCacheService regionEntityCacheService = context.getBean(RegionEntityCacheService.class);
        PlayerItems playerItems = regionEntityCacheService.loadRegionContext(id, Item.class, PlayerItems::new);

        Player player2 = entityCacheService.loadEntity(id, Player.class);
        playerItems = regionEntityCacheService.loadRegionContext(id, Item.class, PlayerItems::new);

        context.close();
    }
}
