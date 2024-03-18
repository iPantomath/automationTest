package com.dhl.demp.dmac.di.module

import com.dhl.demp.dmac.domain.deeplink_parser.DeepLinkParser
import com.dhl.demp.dmac.domain.deeplink_parser.DeepLinkParserImpl
import com.dhl.demp.dmac.domain.utils.DependencyUtils
import com.dhl.demp.dmac.domain.utils.DependencyUtilsImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface CommonUtilsModule {
    @Binds
    fun provideDependencyUtils(utils: DependencyUtilsImpl): DependencyUtils

    @Binds
    fun bindDeepLinkParser(repo: DeepLinkParserImpl): DeepLinkParser
}