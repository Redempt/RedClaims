package redempt.redclaims.claim;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import redempt.redclaims.BlockDisplayer;
import redempt.redclaims.ClaimFlag;
import redempt.redlib.misc.LocationUtils;
import redempt.redlib.region.CuboidRegion;
import redempt.redlib.sql.SQLHelper;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class Subclaim extends Claim {
	
	private Claim parent;
	private CuboidRegion region;
	
	public Subclaim(String name, CuboidRegion region, UUID owner, Claim parent) {
		super(parent.sql, name, null, owner, parent.getFlags());
		this.region = region;
		this.parent = parent;
	}
	
	public Subclaim(SQLHelper sql, String name, CuboidRegion region, UUID owner, Set<ClaimFlag> flags) {
		super(sql, name, null, owner, flags);
		this.region = region;
	}
	
	public void setParent(Claim parent) {
		this.parent = parent;
	}
	
	public Claim getParent() {
		return parent;
	}
	
	@Override
	public CuboidRegion getRegion() {
		return region;
	}

	@Override
	public String getFullName() {
		return parent.getFullName() + ":" + getName();
	}
	
	@Override
	public void setRegion(CuboidRegion region) {
		this.region = region;
		sql.execute("UPDATE claims SET region=? WHERE name=? AND owner=? AND parent=?;", region.toString(), getName(), getOwner().getUniqueId().toString(), parent.getName());
	}
	
	@Override
	public void initQuery() {
		sql.execute("INSERT INTO claims VALUES(?, ?, ?, ?, ?);",
				getOwner().getUniqueId(), getName(), parent.getName(), getFlags().stream().map(ClaimFlag::getName).collect(Collectors.joining(",")), getRegion().toString());
	}
	
	@Override
	protected void visualize(Player player, boolean subclaims, BlockDisplayer displayer) {
		CuboidRegion region = getRegion();
		Location[] corners = region.clone().expand(-1, 0, -1, 0, -1, 0).getCorners();
		for (Location corner : corners) {
			displayer.display(corner.getBlock(), Material.SEA_LANTERN);
			for (BlockFace face : LocationUtils.PRIMARY_BLOCK_FACES) {
				Block rel = corner.getBlock().getRelative(face);
				if (!region.contains(rel)) {
					continue;
				}
				displayer.display(rel.getLocation().getBlock(), Material.END_STONE_BRICKS);
			}
		}
	}
	
	@Override
	protected void updateFlags() {
		sql.execute("UPDATE claims SET flags=? WHERE name=? AND owner=? AND parent=?;",
				getFlags().stream().map(ClaimFlag::getName).collect(Collectors.joining(",")),
				getName(), getOwner().getUniqueId(), parent);
	}
	
}
