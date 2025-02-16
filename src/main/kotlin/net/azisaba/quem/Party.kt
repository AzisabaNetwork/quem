package net.azisaba.quem

import net.azisaba.quem.impl.PartyImpl
import net.azisaba.quem.impl.SoloPartyImpl
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

interface Party: Iterable<Player> {
    companion object {
        val MAX_SIZE
            get() = Quem.pluginConfig.maxPartySize

        private val instances = mutableSetOf<Party>()

        fun create(leader: Player): Party {
            return PartyImpl(leader).also { instances.add(it) }
        }

        fun solo(player: Player): Party {
            return SoloPartyImpl(player).also { instances.add(it) }
        }

        val Player.party: Party?
            get() = instances.firstOrNull { it.isMember(player) }

        fun Player.hasParty(): Boolean {
            return party != null
        }
    }

    class Invite private constructor(val party: Party, val sender: Player, val player: Player) {
        companion object {
            private val instances = mutableSetOf<Invite>()

            val ticks
                get() = Quem.pluginConfig.partyInviteLimit

            val seconds = ticks / 20L

            fun create(party: Party, sender: Player, player: Player): Invite {
                get(party, player)?.let { return it }
                return Invite(party, sender, player).also { instances.add(it) }
            }

            fun get(uuid: UUID): Invite? {
                return instances.firstOrNull { it.uuid == uuid }
            }

            fun get(party: Party, player: Player): Invite? {
                return instances.firstOrNull { it.party == party && it.player == player }
            }

            fun Player.sendPartyInvite(party: Party, sender: Player = party.leader, player: Player): Invite {
                return create(party, sender, player)
            }
        }

        val uuid: UUID = UUID.randomUUID()

        init {
            sender.sendMessage(Component.empty())
            sender.sendMessage(Component.translatable("party.invite.sent", Component.text(player.name), Component.text(seconds)))
            sender.sendMessage(Component.empty())

            player.sendMessage(Component.empty())
            player.sendMessage(Component.translatable("party.invite.received", Component.text(player.name)))
            player.sendMessage(Component.translatable("party.invite.received.description", Component.text(seconds)))
            player.sendMessage(Component.translatable("party.invite.received.accept").color(NamedTextColor.GOLD)
                .clickEvent(ClickEvent.runCommand("/${Quem.PLUGIN_ID}:party accept $uuid")))
            player.sendMessage(Component.empty())

            Bukkit.getScheduler().runTaskLaterAsynchronously(Quem.plugin, object : Runnable {
                override fun run() {
                    if (! instances.contains(this@Invite)) {
                        return
                    }

                    sender.sendMessage(Component.translatable("party.invite.cancelled", Component.text(player.name)))
                    instances.remove(this@Invite)
                }
            }, ticks)
        }

        fun accept() {
            if (MAX_SIZE <= party.size) {
                player.sendMessage(Component.translatable("party.invite.full"))
                return
            }

            player.party?.removeMember(player)

            party.addMember(player)
            instances.remove(this)
        }
    }

    enum class InvitationSetting {
        LEADER,
        ALL
    }

    var quest: Quest?

    var stage: StageLike?

    var leader: Player

    val members: Set<Player>

    var invitationSetting: InvitationSetting

    val size: Int

    fun hasQuest(): Boolean

    fun hasStage(): Boolean

    fun addMember(member: Player)

    fun removeMember(member: Player)

    fun isMember(player: Player?): Boolean

    fun isNotMember(player: Player?): Boolean {
        return ! isMember(player)
    }

    fun hasPermission(type: QuestType): Boolean

    fun hasInvitationPermission(member: Player): Boolean

    fun broadcast(message: Component)

    fun disband() {
        instances.remove(this)
    }
}