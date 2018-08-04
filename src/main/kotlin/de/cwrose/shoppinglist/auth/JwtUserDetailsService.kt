package de.cwrose.shoppinglist.auth

import de.cwrose.shoppinglist.Authority
import de.cwrose.shoppinglist.User
import de.cwrose.shoppinglist.UserRepository
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component


@Component
class JwtUserDetailsService(val userRepository: UserRepository) : UserDetailsService {

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails = userRepository.findByUsername(username).map { user ->
            createUserDetails(user)
        } .orElseThrow {
            UsernameNotFoundException("User not found")
        }

}

private fun createUserDetails(user: User) = JwtUser(user.id!!, user.username!!, user.passwordHash!!, user.active, user.authorities)

class JwtUser(var id: String, private val username: String, private val password: String, private val active: Boolean, private val authorities: Collection<Authority>) : UserDetails {

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = mapToGrantedAuthorities(authorities)

    override fun isEnabled() = active

    override fun isCredentialsNonExpired() = true

    override fun isAccountNonExpired() = true

    override fun isAccountNonLocked() = active
}

private fun mapToGrantedAuthorities(authorities: Collection<Authority>): MutableCollection<GrantedAuthority> {
    return authorities.map { (name) -> SimpleGrantedAuthority(name!!.name) }.toMutableList()
}